package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.SubmitLessonRequest;
import com.example.nihongo_app.dto.response.CancelLessonResponse;
import com.example.nihongo_app.dto.response.RoadmapLessonResponse.Status;
import com.example.nihongo_app.dto.response.StartLessonResponse;
import com.example.nihongo_app.dto.response.StartLessonResponse.StartLessonOption;
import com.example.nihongo_app.dto.response.StartLessonResponse.StartLessonQuestion;
import com.example.nihongo_app.dto.response.SubmitLessonResponse;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.LessonQuestion;
import com.example.nihongo_app.entity.LessonQuestionOption;
import com.example.nihongo_app.entity.Topic;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserExpLog;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.UserLessonProgress.ProgressStatus;
import com.example.nihongo_app.exception.InsufficientEnergyException;
import com.example.nihongo_app.exception.LessonLockedException;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.LessonQuestionOptionRepository;
import com.example.nihongo_app.repository.LessonQuestionRepository;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.TopicRepository;
import com.example.nihongo_app.repository.UserExpLogRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.LessonAttemptService;
import com.example.nihongo_app.service.LessonUnlockPolicy;
import com.example.nihongo_app.service.StreakService;
import com.example.nihongo_app.service.EnergyService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Implementation của {@link LessonAttemptService}.
 *
 * <h3>Nguyên tắc giao dịch</h3>
 * Mỗi API public đều gắn {@code @Transactional} để đảm bảo:
 * <ul>
 *   <li>Cùng lúc trừ năng lượng, upsert progress, ghi log EXP.</li>
 *   <li>Một lệnh lỗi → rollback toàn bộ (không có user "được điểm mà không có log").</li>
 * </ul>
 *
 * <h3>Thuật toán sao (TIMED_REVIEW)</h3>
 * Số sao dựa trên {@code configJson.starThresholds}: một mảng 2 ngưỡng (giây).
 * Nếu thời gian làm bài &le; ngưỡng[0] → 3 sao; &le; ngưỡng[1] → 2 sao; còn lại → 1 sao.
 * Nếu không có config → fallback về công thức đơn giản dựa trên tỉ lệ đúng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LessonAttemptServiceImpl implements LessonAttemptService {

    private final LessonRepository lessonRepository;
    private final LessonQuestionRepository questionRepository;
    private final LessonQuestionOptionRepository optionRepository;
    private final UserLessonProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final UserExpLogRepository expLogRepository;
    private final TopicRepository topicRepository;
    private final LessonUnlockPolicy unlockPolicy;
    private final StreakService streakService;
    private final EnergyService energyService;

    // ============================ START ============================

    @Override
    @Transactional
    public StartLessonResponse startLesson(Long lessonId, Long userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay bai hoc voi id=" + lessonId));
boolean isReplay = false;

        // 1. Kiem tra unlock qua policy (cung thuat toan voi roadmap).
        Status status = unlockPolicy.evaluate(lesson, userId, isFirstTopicOfSystem(lesson));
        if (status == Status.LOCKED) {
            throw new LessonLockedException(
                    "Bai hoc nay chua duoc mo khoa. Hay hoan thanh bai truoc do truoc.");
        }


        // 4. Upsert progress thanh IN_PROGRESS (replay cung reset de bai duoc tinh la dang hoc).
        upsertProgress(userId, lesson, ProgressStatus.IN_PROGRESS, null);

        // 5. Lay bo de thi -> shuffle -> cat lay N cau theo config (questionsPerSession).
        List<LessonQuestion> allQuestions = questionRepository.findAllByLessonIdOrderByIdAsc(lessonId);
        int sliceSize = resolveQuestionsPerSession(lesson);
        List<LessonQuestion> shuffled = shuffle(allQuestions);
        List<LessonQuestion> picked = shuffled.size() <= sliceSize
                ? shuffled
                : shuffled.subList(0, sliceSize);

        List<StartLessonQuestion> questionResponses = new ArrayList<>(picked.size());
        for (LessonQuestion q : picked) {
            List<LessonQuestionOption> options = optionRepository
                    .findAllByQuestionIdOrderByOrderIndexAscIdAsc(q.getId());
            List<LessonQuestionOption> shuffledOptions = shuffle(options);
            List<StartLessonOption> optionResponses = shuffledOptions.stream()
                    .map(o -> StartLessonOption.builder()
                            .optionId(o.getId())
                            .content(o.getOptionText())
                            .imageUrl(o.getImageUrl())
                            .audioUrl(o.getAudioUrl())
                            .metadataJson(o.getMetadataJson())
                            .isCorrect(o.getCorrect())
                            .order(o.getOrderIndex())
                            .build())
                    .toList();
            questionResponses.add(StartLessonQuestion.builder()
                    .questionId(q.getId())
                    .questionType(q.getQuestionType())
                    .content(q.getQuestionText())
                    .audioUrl(q.getAudioUrl())
                    .imageUrl(q.getImageUrl())
                    .metadataJson(q.getMetadataJson())
                    .options(optionResponses)
                    .build());
        }

        return StartLessonResponse.builder()
                .lessonId(lesson.getId())
                .lessonType(lesson.getLessonType())
                .totalEnergyDeducted(0)
                .isReplay(isReplay)
                .questions(questionResponses)
                .build();
    }

    // ============================ SUBMIT ============================

    @Override
    @Transactional
    public SubmitLessonResponse submitLesson(Long lessonId, Long userId, SubmitLessonRequest req) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay bai hoc voi id=" + lessonId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay user voi id=" + userId));

        // Xac dinh day co phai replay khong.
        // 1. Uu tien FE gui len qua request (vi start da set IN_PROGRESS lam DB khong con COMPLETED).
        // 2. Fallback: neu FE khong gui, mac dinh false (lan dau).
        boolean isReplay = Boolean.TRUE.equals(req.getIsReplay());

        // Tinh toan theo loai bai.
        int expGained;
        int starsEarned = 0;
        boolean passed;
        UserExpLog.SourceType sourceType;

        switch (lesson.getLessonType()) {
            case NORMAL -> {
                expGained = resolveNormalExp(lesson);
                passed = true;
                sourceType = UserExpLog.SourceType.NEW_LESSON;
            }
            case TIMED_REVIEW -> {
                expGained = resolveTimedReviewExp(lesson);
                starsEarned = resolveStars(lesson, req);
                passed = true;
                sourceType = UserExpLog.SourceType.REVIEW_LESSON;
            }
            case JUMP_TEST -> {
                passed = req.getHeartsRemaining() != null && req.getHeartsRemaining() > 0;
                expGained = passed ? resolveJumpTestExp(lesson) : 0;
                sourceType = UserExpLog.SourceType.JUMP_TEST;
            }
            default -> throw new IllegalArgumentException(
                    "Loai bai hoc khong ho tro: " + lesson.getLessonType());
        }

        // Replay: giam EXP (vd chi con 30% so voi lan dau).
        // JUMP_TEST replay van duoc tinh la NEW_LESSON/REVIEW_LESSON vi khong con "nhay coc".
        if (isReplay && expGained > 0) {
            double ratio = resolveReplayExpRatio(lesson);
            expGained = Math.max(1, (int) Math.round(expGained * ratio));
 }

// Tinh nang luong theo tung cau tra loi (per-question energy).
energyService.recoverEnergy(userId);
User userForEnergy = userRepository.findById(userId)
    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user voi id=" + userId));
int maxEnergy = Objects.requireNonNullElse(userForEnergy.getMaxEnergy(), 25);
int currentEnergy = Objects.requireNonNullElse(userForEnergy.getCurrentEnergy(), 0);

if (req.getAnswers() != null && !req.getAnswers().isEmpty()) {
    int totalCost = 0;
    int comboCount = Objects.requireNonNullElse(userForEnergy.getComboCount(), 0);
    int comboReward = 0;
    java.util.Random rnd = new java.util.Random();

    for (SubmitLessonRequest.AnswerDto ans : req.getAnswers()) {
        int cost = 1; // base: 1 energy per question
        if (!Boolean.TRUE.equals(ans.getIsCorrect())) {
            cost += rnd.nextInt(2) + 1; // penalty 1-2 for wrong answer
        } else {
            comboCount++;
            if (comboCount == 5) {
                comboReward += rnd.nextInt(5) + 1; // reward 1-5 at combo 5
                comboCount = 0;
            }
        }
        totalCost += cost;
    }

    // Check sufficiency
    if (currentEnergy < totalCost - comboReward) {
        throw new InsufficientEnergyException(
            "Khong du nang luong. Can " + (totalCost - comboReward)
            + ", hien co " + currentEnergy);
    }

    // Apply deduction + reward
    int newEnergy = Math.min(currentEnergy - totalCost + comboReward, maxEnergy);
    userForEnergy.setCurrentEnergy(newEnergy);
    userForEnergy.setComboCount(comboCount);
    userRepository.save(userForEnergy);
}
        // Cap nhat progress + (neu JUMP_TEST pass) danh dau tat ca bai NORMAL trong topic.
        boolean isTopicCompleted = false;
        var priorProgressOpt = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (passed) {
            // Upsert progress cua bai nay thanh COMPLETED.
            // Replay giu starsEarned cu (chi tinh sao moi neu tot hon).
            Integer starsToPersist = starsEarned;
            if (isReplay && priorProgressOpt.isPresent()) {
                int previousStars = priorProgressOpt.get().getStarsEarned() == null
                        ? 0 : priorProgressOpt.get().getStarsEarned();
                starsToPersist = Math.max(previousStars, starsEarned);
            }
            upsertProgress(userId, lesson, ProgressStatus.COMPLETED, starsToPersist);

            // JUMP_TEST pass -> danh dau tat ca NORMAL/TIMED_REVIEW trong topic la COMPLETED.
            if (lesson.getLessonType() == LessonType.JUMP_TEST) {
                markAllInTopicCompleted(userId, lesson.getTopicId());
                isTopicCompleted = true;
            } else {
                isTopicCompleted = isAllLessonsInTopicCompleted(userId, lesson.getTopicId());
            }
        } else {
            // JUMP_TEST fail: progress giu IN_PROGRESS hoac reset ve LOCKED (de user lam lai binh thuong).
            upsertProgress(userId, lesson, ProgressStatus.IN_PROGRESS, 0);
        }

        // Cong EXP + luu log. Replay cung ghi log (vi user van duoc thuong, it hon).
        if (expGained > 0) {
            user.setExp((user.getExp() == null ? 0 : user.getExp()) + expGained);
            userRepository.save(user);

            expLogRepository.save(UserExpLog.builder()
                    .userId(userId)
                    .expGained(expGained)
                    .sourceType(sourceType)
                    .referenceId(lesson.getId())
                    .build());
        }

        if (passed) {
            streakService.checkAndUpdateStreak(userId);
            int streakBonus = calculateStreakBonus(user.getCurrentStreak());
            if (streakBonus > 0) {
                user.setExp((user.getExp() == null ? 0 : user.getExp()) + streakBonus);
                userRepository.save(user);
                expLogRepository.save(UserExpLog.builder()
                        .userId(userId)
                        .expGained(streakBonus)
                        .sourceType(UserExpLog.SourceType.NEW_LESSON)
                        .referenceId(lesson.getId())
                        .build());
            }
        }

        return SubmitLessonResponse.builder()
                .status(passed ? "COMPLETED" : "IN_PROGRESS")
                .expEarned(expGained)
                .starsEarned(starsEarned)
                .isTopicCompleted(isTopicCompleted)
                .message(passed ? "Tuyet voi, ban da hoan thanh bai hoc!" : "Hay tiep tuc co gang!")
                .currentEnergy(java.util.Objects.requireNonNullElse(user.getCurrentEnergy(), 0))
                .build();
    }

    // ============================ CANCEL ============================

    @Override
    @Transactional
    public CancelLessonResponse cancelLesson(Long lessonId, Long userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay bai hoc voi id=" + lessonId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay user voi id=" + userId));

        // Tinh lai entry cost de biet can hoan bao nhieu.
    // Cancel khong hoan energy vi startLesson da khong tru nua.
        return CancelLessonResponse.builder()
                .energyRefunded(0)
                .currentEnergy(java.util.Objects.requireNonNullElse(user.getCurrentEnergy(), 0))
                .status("LOCKED")
                .build();
    }

    // ============================ Helpers ============================

    /**
     * Upsert progress: neu chua co row -> insert, neu co -> update status/stars.
     */
    private void upsertProgress(Long userId, Lesson lesson, ProgressStatus status, Integer stars) {
        UserLessonProgress progress = progressRepository
                .findByUserIdAndLessonId(userId, lesson.getId())
                .orElseGet(() -> UserLessonProgress.builder()
                        .userId(userId)
                        .lessonId(lesson.getId())
                        .starsEarned(0)
                        .build());

        progress.setStatus(status);
        if (stars != null) {
            progress.setStarsEarned(stars);
        }
        if (progress.getUnlockedAt() == null) {
            progress.setUnlockedAt(LocalDateTime.now());
        }
        progressRepository.save(progress);
    }

    /**
     * Tra ve true neu lesson thuoc topic co orderIndex nho nhat toan he thong.
     */
    private boolean isFirstTopicOfSystem(Lesson lesson) {
        List<Topic> topics = topicRepository.findAllActiveWithLessons();
        Integer minOrder = topics.stream()
                .map(Topic::getOrderIndex)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        if (minOrder == null) {
            return false;
        }
        return topics.stream()
                .filter(t -> Objects.equals(t.getOrderIndex(), minOrder))
                .anyMatch(t -> Objects.equals(t.getId(), lesson.getTopicId()));
    }

    /**
     * Tinh entry cost tu configJson.
     * <ul>
     *   <li>Neu configJson.co {@code entryCostEnergy} -> dung no (uu tien).</li>
     *   <li>Neu khong -> dung {@code questionsPerSession} (mac dinh 10) de khop voi so cau user se lam.</li>
     * </ul>
     */
    private int resolveEntryCost(Lesson lesson) {
        JsonNode config = lesson.getConfigJson();
        if (config != null && config.has("entryCostEnergy")) {
            return config.get("entryCostEnergy").asInt();
        }
        return resolveQuestionsPerSession(lesson);
    }

    private int resolveNormalExp(Lesson lesson) {
        JsonNode config = lesson.getConfigJson();
        if (config != null && config.has("expReward")) {
            return config.get("expReward").asInt();
        }
        return 15;
    }

    private int resolveTimedReviewExp(Lesson lesson) {
        JsonNode config = lesson.getConfigJson();
        if (config != null && config.has("expReward")) {
            return config.get("expReward").asInt();
        }
        return 10;
    }

    private int resolveJumpTestExp(Lesson lesson) {
        JsonNode config = lesson.getConfigJson();
        if (config != null && config.has("expReward")) {
            return config.get("expReward").asInt();
        }
        return 30;
    }

    /**
     * Ti le EXP con lai khi user lam lai bai da hoan thanh (replay).
     * Mac dinh 0.3 (30% so voi lan dau) neu khong co config.
     * Admin co the override qua {@code configJson.replayExpRatio}.
     */
    private double resolveReplayExpRatio(Lesson lesson) {
        JsonNode config = lesson.getConfigJson();
        if (config != null && config.has("replayExpRatio")) {
            double ratio = config.get("replayExpRatio").asDouble();
            // Gioi han trong [0, 1] de tranh bug config.
            if (ratio < 0) return 0;
            if (ratio > 1) return 1;
            return ratio;
        }
        return 0.3;
    }

    /**
     * So cau hoi moi khi start. Dung de cat tu pool lon (vd 50 cau -> 10 cau moi lan start).
     * Mac dinh 10 neu khong co config.
     */
    private int resolveQuestionsPerSession(Lesson lesson) {
        JsonNode config = lesson.getConfigJson();
        if (config != null && config.has("questionsPerSession")) {
            int n = config.get("questionsPerSession").asInt();
            return Math.max(1, n);
        }
        return 10;
    }

    /**
     * Tinh so sao (0-3) cho TIMED_REVIEW dua tren timeTakenSeconds va configJson.starThresholds.
     * starThresholds la mang JSON: [thoiGian3Sao, thoiGian2Sao] (giay).
     * Neu khong co config -> fallback theo ti le dung:
     *   >= 90% -> 3 sao, >= 70% -> 2 sao, con lai -> 1 sao.
     */
    private int resolveStars(Lesson lesson, SubmitLessonRequest req) {
        JsonNode config = lesson.getConfigJson();
        int time = req.getTimeTakenSeconds() == null ? Integer.MAX_VALUE : req.getTimeTakenSeconds();
        if (config != null && config.has("starThresholds") && config.get("starThresholds").isArray()) {
            int t3 = config.get("starThresholds").get(0).asInt(Integer.MAX_VALUE);
            int t2 = config.get("starThresholds").size() > 1
                    ? config.get("starThresholds").get(1).asInt(Integer.MAX_VALUE)
                    : Integer.MAX_VALUE;
            if (time <= t3) return 3;
            if (time <= t2) return 2;
            return 1;
        }
        // Fallback theo ti le.
        int total = req.getTotalQuestions() == null ? 0 : req.getTotalQuestions();
        int correct = req.getTotalCorrect() == null ? 0 : req.getTotalCorrect();
        if (total <= 0) return 0;
        double ratio = (double) correct / total;
        if (ratio >= 0.9) return 3;
        if (ratio >= 0.7) return 2;
        return 1;
    }

    /**
     * JUMP_TEST pass -> danh dau tat ca bai NORMAL/TIMED_REVIEW trong topic la COMPLETED.
     */
    private void markAllInTopicCompleted(Long userId, Long topicId) {
        List<Lesson> siblings = lessonRepository.findAllByTopicIdOrdered(topicId);
        for (Lesson sibling : siblings) {
            if (sibling.getLessonType() == LessonType.JUMP_TEST) continue;
            upsertProgress(userId, sibling, ProgressStatus.COMPLETED, null);
        }
    }

    /**
     * Kiem tra toan bo bai (tru JUMP_TEST) trong topic da COMPLETED chua.
     */
    private boolean isAllLessonsInTopicCompleted(Long userId, Long topicId) {
        List<Lesson> siblings = lessonRepository.findAllByTopicIdOrdered(topicId);
        List<UserLessonProgress> userProgresses = progressRepository.findAllByUserId(userId);
        java.util.Map<Long, UserLessonProgress> progressByLesson = new java.util.HashMap<>();
        for (UserLessonProgress p : userProgresses) {
            progressByLesson.put(p.getLessonId(), p);
        }
        for (Lesson sibling : siblings) {
            if (sibling.getLessonType() == LessonType.JUMP_TEST) continue;
            UserLessonProgress progress = progressByLesson.get(sibling.getId());
            if (progress == null || progress.getStatus() != ProgressStatus.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fisher-Yates shuffle dung de dao thu tu cau hoi va dap an.
     * Su dung Math.random() (khong can SecureRandom cho use case game).
     */
    private int calculateStreakBonus(Integer currentStreak) {
        return (currentStreak != null && currentStreak >= 7) ? 5 : 0;
    }

    private <T> List<T> shuffle(List<T> source) {
        List<T> list = new ArrayList<>(source);
        for (int i = list.size() - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
        return list;
    }
}