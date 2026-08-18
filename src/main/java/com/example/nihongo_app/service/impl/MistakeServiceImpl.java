package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.config.MistakeReviewProperties;
import com.example.nihongo_app.dto.request.AnswerItem;
import com.example.nihongo_app.dto.request.ReviewSubmitRequest;
import com.example.nihongo_app.dto.response.MistakeSummaryResponse;
import com.example.nihongo_app.dto.response.ReviewSessionResponse;
import com.example.nihongo_app.dto.response.ReviewSessionResponse.ReviewOption;
import com.example.nihongo_app.dto.response.ReviewSessionResponse.ReviewQuestion;
import com.example.nihongo_app.dto.response.ReviewSubmitResponse;
import com.example.nihongo_app.dto.response.ReviewSubmitResponse.ReviewAnswerResult;
import com.example.nihongo_app.entity.LessonAttemptAnswer;
import com.example.nihongo_app.entity.LessonQuestion;
import com.example.nihongo_app.entity.LessonQuestionOption;
import com.example.nihongo_app.entity.Mistake;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.LessonQuestionOptionRepository;
import com.example.nihongo_app.repository.LessonQuestionRepository;
import com.example.nihongo_app.repository.MistakeRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.EnergyService;
import com.example.nihongo_app.service.MistakeService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MistakeServiceImpl implements MistakeService {

    private final MistakeRepository mistakeRepository;
    private final LessonQuestionRepository questionRepository;
    private final LessonQuestionOptionRepository optionRepository;
    private final UserRepository userRepository;
    private final EnergyService energyService;
    private final MistakeReviewProperties properties;

    // ============================ Phan 2: ghi nhan tu bai hoc thuong ============================

    @Override
    @Transactional
    public void recordFromAnswers(Long userId, List<LessonAttemptAnswer> gradedAnswers) {
        if (gradedAnswers == null || gradedAnswers.isEmpty()) {
            return;
        }
        for (LessonAttemptAnswer answer : gradedAnswers) {
            if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                // Cau dung trong bai hoc thuong: khong dong vao mistake bank.
                continue;
            }
            Mistake mistake = mistakeRepository.findByUserIdAndQuestionId(userId, answer.getQuestionId())
                    .orElseGet(() -> Mistake.builder()
                            .userId(userId)
                            .questionId(answer.getQuestionId())
                            .wrongCount(0)
                            .correctStreak(0)
                            .status(Mistake.Status.ACTIVE)
                            .build());
            applyWrongAnswer(mistake);
        }
    }

    // ============================ Phan 3: phien on tap ============================

    @Override
    @Transactional(readOnly = true)
    public MistakeSummaryResponse getSummary(Long userId) {
        long activeCount = mistakeRepository.countByUserIdAndStatus(userId, Mistake.Status.ACTIVE);
        User user = getUser(userId);
        return MistakeSummaryResponse.builder()
                .activeCount(activeCount)
                .reviewableToday(remainingRewardedSessionsToday(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSessionResponse startReviewSession(Long userId) {
        Pageable pageable = PageRequest.of(0, properties.getSessionSize());
        List<Mistake> topMistakes = mistakeRepository
                .findAllByUserIdAndStatusOrderByWrongCountDescLastWrongAtDesc(userId, Mistake.Status.ACTIVE, pageable);

        if (topMistakes.isEmpty()) {
            return ReviewSessionResponse.builder()
                    .questions(List.of())
                    .message("Ban chua co loi sai nao can on tap, tiep tuc hoc bai moi nhe!")
                    .build();
        }

        List<Mistake> shuffledMistakes = ShuffleUtil.shuffle(topMistakes);
        List<ReviewQuestion> questionResponses = new ArrayList<>(shuffledMistakes.size());
        for (Mistake mistake : shuffledMistakes) {
            questionResponses.add(toReviewQuestion(mistake.getQuestionId()));
        }

        return ReviewSessionResponse.builder()
                .questions(questionResponses)
                .message(null)
                .build();
    }

    @Override
    @Transactional
    public ReviewSubmitResponse submitReviewSession(Long userId, ReviewSubmitRequest request) {
        User user = getUser(userId);
        List<AnswerItem> answers = request.getAnswers();

        List<ReviewAnswerResult> results = new ArrayList<>();
        int resolvedCount = 0;
        int validAnswerCount = 0;

        if (answers != null) {
            for (AnswerItem item : answers) {
                ReviewAnswerResult result = gradeReviewAnswer(userId, item);
                if (result == null) {
                    continue;
                }
                validAnswerCount++;
                results.add(result);
                if (Boolean.TRUE.equals(result.getResolved())) {
                    resolvedCount++;
                }
            }
        }

        int energyRewarded = validAnswerCount > 0 ? grantReviewEnergyRewardIfEligible(user) : 0;

        return ReviewSubmitResponse.builder()
                .results(results)
                .resolvedCount(resolvedCount)
                .energyRewarded(energyRewarded)
                // energyService.addEnergy() ben duoi chay chung transaction (REQUIRED, mac dinh)
                // nen cung 1 Hibernate session -> findById lai cung userId tra ve dung instance
                // `user` nay, currentEnergy da duoc cap nhat san khong can doc lai tu DB.
                .currentEnergy(user.getCurrentEnergy())
                .build();
    }

    // ============================ Helpers ============================

    private ReviewQuestion toReviewQuestion(Long questionId) {
        LessonQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay cau hoi voi id=" + questionId));

        List<LessonQuestionOption> options = optionRepository
                .findAllByQuestionIdOrderByOrderIndexAscIdAsc(question.getId());
        List<LessonQuestionOption> shuffledOptions = ShuffleUtil.shuffle(options);
        List<ReviewOption> optionResponses = shuffledOptions.stream()
                .map(o -> ReviewOption.builder()
                        .optionId(o.getId())
                        .content(o.getOptionText())
                        .imageUrl(o.getImageUrl())
                        .audioUrl(o.getAudioUrl())
                        .metadataJson(o.getMetadataJson())
                        .order(o.getOrderIndex())
                        .build())
                .toList();

        return ReviewQuestion.builder()
                .questionId(question.getId())
                .questionType(question.getQuestionType())
                .content(question.getQuestionText())
                .audioUrl(question.getAudioUrl())
                .imageUrl(question.getImageUrl())
                .metadataJson(question.getMetadataJson())
                .options(optionResponses)
                .build();
    }

    /**
     * Cham 1 cau tra loi trong phien on: doi chieu selectedOptionId voi DB de tu xac dinh
     * dung/sai (khong tin FE), roi cap nhat Mistake tuong ung. Tra ve {@code null} (bo qua,
     * co log warning) neu answer thieu du lieu, option khong thuoc question, hoac user
     * khong co mistake nao ung voi questionId nay (khong tao moi mistake tu phien on).
     */
    private ReviewAnswerResult gradeReviewAnswer(Long userId, AnswerItem item) {
        if (item.getQuestionId() == null || item.getSelectedOptionId() == null) {
            log.warn("Bo qua review answer thieu questionId/selectedOptionId: user={}", userId);
            return null;
        }

        LessonQuestionOption selectedOption = optionRepository.findById(item.getSelectedOptionId()).orElse(null);
        if (selectedOption == null || !Objects.equals(selectedOption.getQuestionId(), item.getQuestionId())) {
            log.warn("Bo qua review answer khong hop le (option khong thuoc question): user={} "
                            + "questionId={} selectedOptionId={}",
                    userId, item.getQuestionId(), item.getSelectedOptionId());
            return null;
        }

        Mistake mistake = mistakeRepository.findByUserIdAndQuestionId(userId, item.getQuestionId()).orElse(null);
        if (mistake == null) {
            log.warn("Bo qua review answer: user={} khong co mistake nao voi questionId={}",
                    userId, item.getQuestionId());
            return null;
        }

        boolean correct = Boolean.TRUE.equals(selectedOption.getCorrect());
        boolean resolved;
        if (correct) {
            resolved = applyCorrectAnswer(mistake);
        } else {
            applyWrongAnswer(mistake);
            resolved = false;
        }

        Long correctOptionId = optionRepository.findFirstByQuestionIdAndCorrectTrue(item.getQuestionId())
                .map(LessonQuestionOption::getId)
                .orElse(null);

        return ReviewAnswerResult.builder()
                .questionId(item.getQuestionId())
                .correct(correct)
                .correctOptionId(correctOptionId)
                .resolved(resolved)
                .build();
    }

    /**
     * Cau sai (dung chung cho ca Phan 2 va phien on): wrong_count++, reset correct_streak,
     * mo lai ACTIVE (ke ca dang RESOLVED), cap nhat last_wrong_at.
     */
    private void applyWrongAnswer(Mistake mistake) {
        mistake.setWrongCount(Objects.requireNonNullElse(mistake.getWrongCount(), 0) + 1);
        mistake.setCorrectStreak(0);
        mistake.setStatus(Mistake.Status.ACTIVE);
        mistake.setLastWrongAt(LocalDateTime.now());
        mistakeRepository.save(mistake);
    }

    /**
     * Cau dung trong phien on: correct_streak++, last_correct_at = now. Neu streak dat nguong
     * VA lan dung truoc do (last_correct_at cu) cach lan nay >= resolve-min-gap-days ngay
     * -> chuyen RESOLVED ("xoa no"). Dieu kien nay chan hoc vet: 2 lan dung lien tiep trong
     * cung 1 phien/ngay khong du de xoa no, phai cach nhau it nhat 1 ngay.
     *
     * @return true neu mistake vua chuyen sang RESOLVED do lan tra loi nay.
     */
    private boolean applyCorrectAnswer(Mistake mistake) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previousCorrectAt = mistake.getLastCorrectAt();
        int newStreak = Objects.requireNonNullElse(mistake.getCorrectStreak(), 0) + 1;

        mistake.setCorrectStreak(newStreak);
        mistake.setLastCorrectAt(now);

        boolean justResolved = mistake.getStatus() == Mistake.Status.ACTIVE
                && newStreak >= properties.getResolveStreak()
                && previousCorrectAt != null
                && Duration.between(previousCorrectAt, now).toDays() >= properties.getResolveMinGapDays();
        if (justResolved) {
            mistake.setStatus(Mistake.Status.RESOLVED);
        }
        mistakeRepository.save(mistake);
        return justResolved;
    }

    /**
     * Thuong nang luong cho 1 phien on hop le (>=1 cau da cham), toi da
     * {@code rewarded-sessions-per-day} lan/ngay — dem qua 2 cot tren User, tu reset khi
     * sang ngay moi (khong can job rieng).
     *
     * @return so nang luong da thuong (0 neu da het luot thuong trong ngay).
     */
    private int grantReviewEnergyRewardIfEligible(User user) {
        boolean sameDay = Objects.equals(user.getLastMistakeReviewRewardDate(), LocalDate.now());
        int usedToday = sameDay ? Objects.requireNonNullElse(user.getMistakeReviewRewardCountToday(), 0) : 0;
        if (usedToday >= properties.getRewardedSessionsPerDay()) {
            return 0;
        }

        user.setLastMistakeReviewRewardDate(LocalDate.now());
        user.setMistakeReviewRewardCountToday(usedToday + 1);
        userRepository.save(user);

        int reward = properties.getEnergyReward();
        energyService.addEnergy(user.getId(), reward);
        return reward;
    }

    private int remainingRewardedSessionsToday(User user) {
        boolean sameDay = Objects.equals(user.getLastMistakeReviewRewardDate(), LocalDate.now());
        int usedToday = sameDay ? Objects.requireNonNullElse(user.getMistakeReviewRewardCountToday(), 0) : 0;
        return Math.max(0, properties.getRewardedSessionsPerDay() - usedToday);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user voi id=" + userId));
    }
}
