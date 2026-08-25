package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.VocabularyReviewRequest;
import com.example.nihongo_app.dto.response.VocabularyDueResponse;
import com.example.nihongo_app.dto.response.VocabularyReviewResponse;
import com.example.nihongo_app.dto.response.VocabularyItemResponse;
import com.example.nihongo_app.entity.LessonAttemptAnswer;
import com.example.nihongo_app.entity.UserVocabularyProgress;
import com.example.nihongo_app.entity.Vocabulary;
import com.example.nihongo_app.repository.UserVocabularyProgressRepository;
import com.example.nihongo_app.repository.VocabularyRepository;
import com.example.nihongo_app.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final UserVocabularyProgressRepository progressRepository;
    private final Sm2Scheduler scheduler;

    @Override
    @Transactional
    public void recordFromAnswers(Long userId, List<LessonAttemptAnswer> gradedAnswers) {
        if (gradedAnswers == null || gradedAnswers.isEmpty()) {
            return;
        }

        List<Long> questionIds = gradedAnswers.stream()
                .map(LessonAttemptAnswer::getQuestionId)
                .distinct()
                .toList();

        // question_id -> các từ trọng tâm của câu đó
        Map<Long, List<Long>> vocabByQuestion = new HashMap<>();
        for (Object[] row : vocabularyRepository.findTargetVocabularyByQuestionIds(questionIds)) {
            Long questionId = ((Number) row[0]).longValue();
            Long vocabularyId = ((Number) row[1]).longValue();
            vocabByQuestion.computeIfAbsent(questionId, k -> new ArrayList<>()).add(vocabularyId);
        }
        if (vocabByQuestion.isEmpty()) {
            // Bài học chưa được gắn từ nào (nội dung nhập qua API admin chẳng hạn).
            // Không phải lỗi: chỉ là lượt này không đẩy được lịch ôn.
            return;
        }

        // Một từ có thể là trọng tâm của NHIỀU câu trong cùng một lượt làm bài. Gộp
        // trước rồi mới áp lịch: nếu áp từng câu một, cùng một từ sẽ bị SM-2 nhân
        // khoảng cách nhiều lần liên tiếp và văng ra tận vài tháng chỉ sau một buổi học.
        // Sai ở BẤT KỲ câu nào cũng tính là chưa nhớ — đó là kết quả thận trọng hơn.
        Map<Long, Boolean> correctByVocabulary = new LinkedHashMap<>();
        for (LessonAttemptAnswer answer : gradedAnswers) {
            List<Long> vocabularyIds = vocabByQuestion.get(answer.getQuestionId());
            if (vocabularyIds == null) {
                continue;
            }
            boolean correct = Boolean.TRUE.equals(answer.getIsCorrect());
            for (Long vocabularyId : vocabularyIds) {
                correctByVocabulary.merge(vocabularyId, correct, (a, b) -> a && b);
            }
        }
        if (correctByVocabulary.isEmpty()) {
            return;
        }

        List<Long> touched = List.copyOf(correctByVocabulary.keySet());
        Map<Long, UserVocabularyProgress> existing = new HashMap<>();
        for (UserVocabularyProgress p : progressRepository.findAllByUserIdAndVocabularyIdIn(userId, touched)) {
            existing.put(p.getVocabularyId(), p);
        }

        LocalDateTime now = LocalDateTime.now();
        List<UserVocabularyProgress> toSave = new ArrayList<>(correctByVocabulary.size());
        for (Map.Entry<Long, Boolean> entry : correctByVocabulary.entrySet()) {
            UserVocabularyProgress progress = existing.get(entry.getKey());
            if (progress == null) {
                progress = scheduler.newProgress(userId, entry.getKey());
            }
            boolean correct = entry.getValue();

            // Trả lời ĐÚNG khi từ CHƯA tới hạn thì không đẩy lịch đi xa thêm.
            //
            // Không có chốt này, người học làm lại cùng một bài năm lần trong một buổi
            // là khoảng cách ôn của từ đó bị nhân với hệ số dễ nhớ năm lần liên tiếp và
            // văng ra tận vài tháng — trong khi họ mới chỉ nhớ được trong vòng mười phút.
            // Nhớ lại một thứ vừa mới nhìn thấy gần như không củng cố gì thêm (chính là
            // lý do "học dồn" không vào), nên bỏ qua ở đây mới đúng cả về mặt thuật toán
            // lẫn về mặt học tập.
            //
            // Trả lời SAI thì LUÔN áp: quên là tin tức thật, bất kể lịch nói gì.
            boolean beforeDue = progress.getNextDueAt() != null
                    && progress.getNextDueAt().isAfter(now);
            if (correct && beforeDue) {
                progress.setTotalCorrect(progress.getTotalCorrect() + 1);
                progress.setLastReviewedAt(now);
                toSave.add(progress);
                continue;
            }

            scheduler.apply(progress, scheduler.qualityOf(correct), now);
            toSave.add(progress);
        }
        progressRepository.saveAll(toSave);
    }

    @Override
    @Transactional
    public VocabularyReviewResponse submitReview(Long userId, VocabularyReviewRequest request) {
        List<VocabularyReviewRequest.Item> results =
                request == null || request.getResults() == null ? List.of() : request.getResults();

        LocalDateTime now = LocalDateTime.now();

        // Gộp trùng như bên recordFromAnswers: một từ lỡ xuất hiện hai lần trong cùng
        // phiên thì chỉ áp lịch một lần, và sai ở bất kỳ lần nào cũng tính là chưa nhớ.
        Map<Long, Boolean> byVocabulary = new LinkedHashMap<>();
        for (VocabularyReviewRequest.Item item : results) {
            if (item == null || item.getVocabularyId() == null) {
                continue;
            }
            byVocabulary.merge(item.getVocabularyId(), Boolean.TRUE.equals(item.getCorrect()),
                    (a, b) -> a && b);
        }

        int correctCount = 0;
        if (!byVocabulary.isEmpty()) {
            List<Long> ids = List.copyOf(byVocabulary.keySet());
            Map<Long, UserVocabularyProgress> existing = new HashMap<>();
            for (UserVocabularyProgress p : progressRepository.findAllByUserIdAndVocabularyIdIn(userId, ids)) {
                existing.put(p.getVocabularyId(), p);
            }

            List<UserVocabularyProgress> toSave = new ArrayList<>(byVocabulary.size());
            for (Map.Entry<Long, Boolean> entry : byVocabulary.entrySet()) {
                UserVocabularyProgress progress = existing.get(entry.getKey());
                if (progress == null) {
                    progress = scheduler.newProgress(userId, entry.getKey());
                }
                boolean correct = entry.getValue();
                if (correct) {
                    correctCount++;
                }
                // KHÔNG có chốt "chưa tới hạn" như ở recordFromAnswers: phiên ôn vốn chỉ
                // gồm những từ ĐÃ tới hạn, và người học chủ động vào ôn thì kết quả của
                // họ phải được tính.
                scheduler.apply(progress, scheduler.qualityOf(correct), now);
                toSave.add(progress);
            }
            progressRepository.saveAll(toSave);
        }

        return VocabularyReviewResponse.builder()
                .reviewedCount(byVocabulary.size())
                .correctCount(correctCount)
                .remainingDue(progressRepository.countByUserIdAndNextDueAtLessThanEqual(userId, LocalDateTime.now()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> resolveNewQuestions(Long userId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<Long>> vocabByQuestion = new HashMap<>();
        for (Object[] row : vocabularyRepository.findTargetVocabularyByQuestionIds(questionIds)) {
            Long questionId = ((Number) row[0]).longValue();
            Long vocabularyId = ((Number) row[1]).longValue();
            vocabByQuestion.computeIfAbsent(questionId, k -> new ArrayList<>()).add(vocabularyId);
        }
        if (vocabByQuestion.isEmpty()) {
            return Map.of();
        }

        List<Long> allVocabIds = vocabByQuestion.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

        Set<Long> alreadySeen = new HashSet<>();
        for (UserVocabularyProgress p : progressRepository.findAllByUserIdAndVocabularyIdIn(userId, allVocabIds)) {
            if (p.getFirstLearnedAt() != null) {
                alreadySeen.add(p.getVocabularyId());
            }
        }

        Map<Long, Boolean> out = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : vocabByQuestion.entrySet()) {
            // "Mới" = CÒN ÍT NHẤT MỘT từ trọng tâm chưa gặp. Câu ghép nhiều từ mà chỉ
            // một từ lạ thì vẫn đáng gắn nhãn — đó chính là từ người học cần chú ý.
            boolean isNew = entry.getValue().stream().anyMatch(id -> !alreadySeen.contains(id));
            out.put(entry.getKey(), isNew);
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyDueResponse getDue(Long userId, int limit) {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, sanitizeLimit(limit));

        List<UserVocabularyProgress> due = progressRepository
                .findAllByUserIdAndNextDueAtLessThanEqualOrderByNextDueAtAsc(userId, now, pageable);

        return VocabularyDueResponse.builder()
                .dueCount(progressRepository.countByUserIdAndNextDueAtLessThanEqual(userId, now))
                .learnedCount(progressRepository.countByUserIdAndFirstLearnedAtIsNotNull(userId))
                .items(toItems(due, now))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyDueResponse getLearned(Long userId, int limit) {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, sanitizeLimit(limit));

        List<UserVocabularyProgress> learned = progressRepository
                .findAllByUserIdAndFirstLearnedAtIsNotNullOrderByFirstLearnedAtDesc(userId, pageable);

        return VocabularyDueResponse.builder()
                .dueCount(progressRepository.countByUserIdAndNextDueAtLessThanEqual(userId, now))
                .learnedCount(progressRepository.countByUserIdAndFirstLearnedAtIsNotNull(userId))
                .items(toItems(learned, now))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocabularyItemResponse> getGlossary() {
        return vocabularyRepository.findAllByOrderByIdAsc().stream()
                .map(v -> toItem(v, null, null))
                .toList();
    }

    // ============================ Helpers ============================

    private List<VocabularyItemResponse> toItems(List<UserVocabularyProgress> progresses, LocalDateTime now) {
        if (progresses.isEmpty()) {
            return List.of();
        }
        List<Long> ids = progresses.stream().map(UserVocabularyProgress::getVocabularyId).toList();
        Map<Long, Vocabulary> words = new HashMap<>();
        for (Vocabulary v : vocabularyRepository.findAllById(ids)) {
            words.put(v.getId(), v);
        }

        List<VocabularyItemResponse> out = new ArrayList<>(progresses.size());
        for (UserVocabularyProgress p : progresses) {
            Vocabulary v = words.get(p.getVocabularyId());
            // Từ bị xoá khỏi kho nhưng tiến độ còn sót: bỏ qua thay vì trả về null cho client.
            if (v == null) {
                continue;
            }
            out.add(toItem(v, p, now));
        }
        return out;
    }

    private VocabularyItemResponse toItem(Vocabulary v, UserVocabularyProgress p, LocalDateTime now) {
        return VocabularyItemResponse.builder()
                .id(v.getId())
                .itemType(v.getItemType() == null ? null : v.getItemType().name())
                .surface(v.getSurface())
                .reading(v.getReading())
                .romaji(v.getRomaji())
                .meaningVn(v.getMeaningVn())
                .audioUrl(v.getAudioUrl())
                .firstLearnedAt(p == null ? null : p.getFirstLearnedAt())
                .nextDueAt(p == null ? null : p.getNextDueAt())
                .repetitions(p == null ? null : p.getRepetitions())
                .due(p != null && p.getNextDueAt() != null && now != null
                        && !p.getNextDueAt().isAfter(now))
                .build();
    }

    /** Chặn client hỏi 100k dòng một lần. */
    private int sanitizeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, 100);
    }
}
