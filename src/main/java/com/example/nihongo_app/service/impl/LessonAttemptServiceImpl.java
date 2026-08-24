package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.AnswerItem;
import com.example.nihongo_app.dto.request.SubmitLessonRequest;
import com.example.nihongo_app.dto.response.CancelLessonResponse;
import com.example.nihongo_app.dto.response.RoadmapLessonResponse.Status;
import com.example.nihongo_app.dto.response.StartLessonResponse;
import com.example.nihongo_app.dto.response.StartLessonResponse.StartLessonOption;
import com.example.nihongo_app.dto.response.StartLessonResponse.StartLessonQuestion;
import com.example.nihongo_app.dto.response.SubmitLessonResponse;
import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.CoinTransaction.TransactionType;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.LessonQuestion;
import com.example.nihongo_app.entity.LessonQuestionOption;
import com.example.nihongo_app.entity.QuestDefinition.QuestType;
import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserExpLog;
import com.example.nihongo_app.entity.LessonAttemptAnswer;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.UserLessonProgress.ProgressStatus;
import com.example.nihongo_app.exception.InsufficientEnergyException;
import com.example.nihongo_app.exception.LessonLockedException;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.CoinTransactionRepository;
import com.example.nihongo_app.repository.LessonAttemptAnswerRepository;
import com.example.nihongo_app.repository.LessonQuestionOptionRepository;
import com.example.nihongo_app.repository.LessonQuestionRepository;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.RankRepository;
import com.example.nihongo_app.repository.UserExpLogRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.AchievementProgress;
import com.example.nihongo_app.service.AchievementService;
import com.example.nihongo_app.service.DailyQuestService;
import com.example.nihongo_app.service.LessonAttemptService;
import com.example.nihongo_app.service.LessonUnlockPolicy;
import com.example.nihongo_app.service.MistakeService;
import com.example.nihongo_app.service.ShopService;
import com.example.nihongo_app.service.StreakService;
import com.example.nihongo_app.service.EnergyService;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final RankRepository rankRepository;
    private final UserExpLogRepository expLogRepository;
    private final LessonUnlockPolicy unlockPolicy;
    private final StreakService streakService;
    private final EnergyService energyService;
    private final CoinTransactionRepository coinTransactionRepository;
    private final DailyQuestService dailyQuestService;
    private final ShopService shopService;
    private final LessonAttemptAnswerRepository lessonAttemptAnswerRepository;
    private final MistakeService mistakeService;
    private final AchievementService achievementService;

    private static final int NORMAL_COIN_BASE = 8;
    private static final int NORMAL_COIN_PERFECT_BONUS = 4;
    private static final int TIMED_REVIEW_COIN_BASE = 6;
    private static final int TIMED_REVIEW_COIN_PERFECT_BONUS = 3;
    private static final int JUMP_TEST_COIN_REWARD = 20;
    private static final int STREAK_MILESTONE_7_BONUS = 50;
    private static final int STREAK_MILESTONE_30_BONUS = 200;
    private static final int STREAK_MILESTONE_100_BONUS = 1000;
    /** Chi phi nang luong mac dinh de vao 1 bai hoc (khop voi max_energy mac dinh 25). */
    private static final int DEFAULT_ENTRY_COST_ENERGY = 10;
    /** Bai hoan hao (0 sai) hoan lai 5 nang luong -> ton rong 5/25 (co the lam 5 bai/ngay). */
    private static final int PERFECT_LESSON_ENERGY_REFUND = 5;
    /** Bai kha (<=2 loi, nhung khong phai hoan hao) hoan lai 2 nang luong -> ton rong 8. */
    private static final int GOOD_LESSON_ENERGY_REFUND = 2;
    private static final int GOOD_LESSON_MAX_MISTAKES = 2;

    // ============================ START ============================

    @Override
    @Transactional
    public StartLessonResponse startLesson(Long lessonId, Long userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay bai hoc voi id=" + lessonId));

        // 1. Kiem tra unlock qua policy (cung thuat toan voi roadmap).
        Status status = unlockPolicy.evaluate(lesson, userId);
        if (status == Status.LOCKED) {
            throw new LessonLockedException(
                    "Bai hoc nay chua duoc mo khoa. Hay hoan thanh bai truoc do truoc.");
        }

        // 2. Phan nhanh replay: bai da COMPLETED -> cho phep lam lai, mien phi energy,
        //    EXP se bi giam khi submit (xem resolveExpXxx). Van giu starsEarned cu (neu co).
        boolean isReplay = (status == Status.COMPLETED);
        int totalEnergy = isReplay ? 0 : resolveEntryCost(lesson);

        // 3. Tru nang luong user (chi khi lan dau, replay khong mat energy).
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay user voi id=" + userId));
        if (!isReplay) {
            if (user.getCurrentEnergy() == null || user.getCurrentEnergy() < totalEnergy) {
                throw new InsufficientEnergyException(
                        "Khong du nang luong. Can " + totalEnergy
                                + " nang luong, hien co " + (user.getCurrentEnergy() == null ? 0 : user.getCurrentEnergy()));
            }
            user.setCurrentEnergy(user.getCurrentEnergy() - totalEnergy);
            userRepository.save(user);
        }

        // 4. Upsert progress thanh IN_PROGRESS (replay cung reset de bai duoc tinh la dang hoc).
        upsertProgress(userId, lesson, ProgressStatus.IN_PROGRESS, null);

        // 5. Lay bo de thi -> shuffle -> cat lay N cau theo config (questionsPerSession).
        List<LessonQuestion> allQuestions = questionRepository.findAllByLessonIdOrderByIdAsc(lessonId);
        int sliceSize = resolveQuestionsPerSession(lesson);
        List<LessonQuestion> shuffled = ShuffleUtil.shuffle(allQuestions);
        List<LessonQuestion> picked = shuffled.size() <= sliceSize
                ? shuffled
                : shuffled.subList(0, sliceSize);

        List<StartLessonQuestion> questionResponses = new ArrayList<>(picked.size());
        for (LessonQuestion q : picked) {
            List<LessonQuestionOption> options = optionRepository
                    .findAllByQuestionIdOrderByOrderIndexAscIdAsc(q.getId());
            List<LessonQuestionOption> shuffledOptions = ShuffleUtil.shuffle(options);
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
                .totalEnergyDeducted(totalEnergy)
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

        // Check active powerups BEFORE calculating base rewards
        boolean hasDoubleXp = shopService.hasActivePowerup(userId, ShopItem.EffectType.DOUBLE_XP);
        boolean hasDoubleCoin = shopService.hasActivePowerup(userId, ShopItem.EffectType.DOUBLE_COIN);
        // Timer boost is handled differently (extends time limit for TIMED_REVIEW)
        boolean hasTimerBoost = shopService.hasActivePowerup(userId, ShopItem.EffectType.TIMER_BOOST);

        // Tinh toan theo loai bai.
        int expGained;
        int coinsGained;
        int starsEarned = 0;
        boolean passed;
        UserExpLog.SourceType sourceType;
        boolean perfectLesson = req.getTotalMistakes() != null && req.getTotalMistakes() == 0;

        switch (lesson.getLessonType()) {
            case NORMAL -> {
                expGained = resolveNormalExp(lesson);
                coinsGained = NORMAL_COIN_BASE + (perfectLesson ? NORMAL_COIN_PERFECT_BONUS : 0);
                passed = true;
                sourceType = UserExpLog.SourceType.NEW_LESSON;
            }
            case TIMED_REVIEW -> {
                expGained = resolveTimedReviewExp(lesson);
                coinsGained = TIMED_REVIEW_COIN_BASE + (perfectLesson ? TIMED_REVIEW_COIN_PERFECT_BONUS : 0);
                starsEarned = resolveStars(lesson, req);
                passed = true;
                sourceType = UserExpLog.SourceType.REVIEW_LESSON;
            }
            case JUMP_TEST -> {
                passed = req.getHeartsRemaining() != null && req.getHeartsRemaining() > 0;
                expGained = passed ? resolveJumpTestExp(lesson) : 0;
                coinsGained = passed ? JUMP_TEST_COIN_REWARD : 0;
                sourceType = UserExpLog.SourceType.JUMP_TEST;
            }
            default -> throw new IllegalArgumentException(
                    "Loai bai hoc khong ho tro: " + lesson.getLessonType());
        }

        // Replay: giam EXP + coin (vd chi con 30% so voi lan dau), tranh farm bang cach lam lai lien tuc.
        // JUMP_TEST replay van duoc tinh la NEW_LESSON/REVIEW_LESSON vi khong con "nhay coc".
        if (isReplay) {
            double ratio = resolveReplayExpRatio(lesson);
            if (expGained > 0) {
                expGained = Math.max(1, (int) Math.round(expGained * ratio));
            }
            if (coinsGained > 0) {
                coinsGained = Math.max(1, (int) Math.round(coinsGained * ratio));
            }
        }

        // Apply powerup multipliers (Double XP, Double Coin)
        if (hasDoubleXp && expGained > 0) {
            expGained *= 2;
        }
        if (hasDoubleCoin && coinsGained > 0) {
            coinsGained *= 2;
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
            user.setLastLearningAt(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
            user.setLastRankDecayAt(null);
            user.setLastRankReminderAt(null);
            userRepository.save(user);

            expLogRepository.save(UserExpLog.builder()
                    .userId(userId)
                    .expGained(expGained)
                    .sourceType(sourceType)
                    .referenceId(lesson.getId())
                    .build());
        }

        if (passed) {
            // Cong coin nhan duoc + ghi log giao dich.
            if (coinsGained > 0) {
                user.setCoins(Objects.requireNonNullElse(user.getCoins(), 0) + coinsGained);
                userRepository.save(user);
                coinTransactionRepository.save(CoinTransaction.builder()
                        .userId(userId)
                        .amount(coinsGained)
                        .transactionType(TransactionType.EARN_LESSON)
                        .referenceId(lesson.getId())
                        .build());
            }

            // Hoan mot phan nang luong da tru luc /start neu lam bai tot (khong ap dung JUMP_TEST
            // hearts, chi dua tren totalMistakes chung cho ca 3 loai bai).
            // Cap o entryCostCharged (0 neu la replay vi replay khong tru gi ca luc start) de
            // chan exploit "replay bai da hoan thanh, lam hoan hao -> farm nang luong mien phi".
            boolean goodLesson = !perfectLesson
                    && req.getTotalMistakes() != null && req.getTotalMistakes() <= GOOD_LESSON_MAX_MISTAKES;
            int entryCostCharged = isReplay ? 0 : resolveEntryCost(lesson);
            int energyRefund = perfectLesson ? PERFECT_LESSON_ENERGY_REFUND
                    : (goodLesson ? GOOD_LESSON_ENERGY_REFUND : 0);
            energyRefund = Math.min(energyRefund, entryCostCharged);
            if (energyRefund > 0) {
                int currentEnergy = Objects.requireNonNullElse(user.getCurrentEnergy(), 0);
                int maxEnergy = user.getMaxEnergy() == null ? currentEnergy + energyRefund : user.getMaxEnergy();
                user.setCurrentEnergy(Math.min(currentEnergy + energyRefund, maxEnergy));
                userRepository.save(user);
            }

            // Cap nhat tien do Daily Quest.
            dailyQuestService.recordProgress(userId, QuestType.COMPLETE_LESSONS, 1);
            if (req.getTotalCorrect() != null && req.getTotalCorrect() > 0) {
                dailyQuestService.recordProgress(userId, QuestType.CORRECT_ANSWERS, req.getTotalCorrect());
            }
            if (perfectLesson) {
                dailyQuestService.recordProgress(userId, QuestType.PERFECT_LESSON, 1);
            }

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

            // Thuong coin khi vua dat moc streak (7/30/100 ngay) - rieng biet voi streakBonus EXP o tren.
            int updatedStreak = Objects.requireNonNullElse(user.getCurrentStreak(), 0);
            int milestoneCoinBonus = switch (updatedStreak) {
                case 7 -> STREAK_MILESTONE_7_BONUS;
                case 30 -> STREAK_MILESTONE_30_BONUS;
                case 100 -> STREAK_MILESTONE_100_BONUS;
                default -> 0;
            };
            if (milestoneCoinBonus > 0) {
                user.setCoins(Objects.requireNonNullElse(user.getCoins(), 0) + milestoneCoinBonus);
                userRepository.save(user);
                coinTransactionRepository.save(CoinTransaction.builder()
                        .userId(userId)
                        .amount(milestoneCoinBonus)
                        .transactionType(TransactionType.STREAK_BONUS)
                        .build());
            }

            // Emit achievement events (không ảnh hưởng tới phần thưởng hiện tại).
            achievementService.onEvent(userId,
                    AchievementProgress.of(AchievementProgress.EventType.LESSONS_COMPLETED.name(), 1, null));
            if (perfectLesson) {
                achievementService.onEvent(userId,
                        AchievementProgress.of(AchievementProgress.EventType.PERFECT_LESSON.name(), 1, null));
            }
            if (isTopicCompleted) {
                achievementService.onEvent(userId,
                        AchievementProgress.of(AchievementProgress.EventType.TOPIC_COMPLETED.name(), 1, null));
            }
            achievementService.onEvent(userId,
                    AchievementProgress.of(AchievementProgress.EventType.COIN_EARNED.name(),
                            Objects.requireNonNullElse(user.getCoins(), 0), null));
            // DAILY_STUDY: học vào giờ hiện tại (Asia/Ho_Chi_Minh) — check EARLY_BIRD/NIGHT_OWL.
            int studyHour = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).getHour();
            achievementService.onEvent(userId,
                    AchievementProgress.of(AchievementProgress.EventType.DAILY_STUDY.name(), 1,
                            String.valueOf(studyHour)));
        }

        // Ghi lai dap an tung cau (Mistake Bank) - khong anh huong toi thuong EXP/Coin/Energy
        // o tren, van tinh theo totalCorrect/totalMistakes FE gui nhu cu.
        List<LessonAttemptAnswer> gradedAnswers = recordAnswers(lesson, userId, req.getAnswers());

        // Cap nhat Mistake Bank: chi cho bai hoc thuong (khong tinh JUMP_TEST) va khong tinh
        // replay (replay lai bai da hoc khong nen tao/lam nang them mistake moi).
        boolean eligibleForMistakeBank = !isReplay
                && (lesson.getLessonType() == LessonType.NORMAL || lesson.getLessonType() == LessonType.TIMED_REVIEW);
        if (eligibleForMistakeBank && !gradedAnswers.isEmpty()) {
            mistakeService.recordFromAnswers(userId, gradedAnswers);
        }

        Rank updatedRank = resolveHighestQualifyingRank(user.getExp());
        boolean promoted = false;
        String newRankName = null;
        if (updatedRank != null) {
            Rank currentRank = user.getRank();
            if (currentRank == null || !Objects.equals(currentRank.getId(), updatedRank.getId())) {
                user.setRank(updatedRank);
                userRepository.save(user);
                promoted = true;
                newRankName = updatedRank.getName();
            } else {
                newRankName = updatedRank.getName();
            }
        }

        return SubmitLessonResponse.builder()
                .status(passed ? "COMPLETED" : "IN_PROGRESS")
                .expEarned(expGained)
                .coinsEarned(passed ? coinsGained : 0)
                .starsEarned(starsEarned)
                .isTopicCompleted(isTopicCompleted)
                .message(passed ? "Tuyet voi, ban da hoan thanh bai hoc!" : "Hay tiep tuc co gang!")
                .isPromoted(promoted)
                .newRankName(newRankName)
                .currentEnergy(user.getCurrentEnergy())
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
        int refund = resolveEntryCost(lesson);

        // Chi hoan neu progress dang IN_PROGRESS (lan truoc user da /start nhung chua /submit).
        // Neu da COMPLETED (kha nang user goi cancel nham), khong lam gi ca.
        var progressOpt = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        boolean needRefund = progressOpt.isPresent()
                && progressOpt.get().getStatus() == ProgressStatus.IN_PROGRESS;

        if (needRefund) {
            int currentEnergy = user.getCurrentEnergy() == null ? 0 : user.getCurrentEnergy();
            int maxEnergy = user.getMaxEnergy() == null ? currentEnergy + refund : user.getMaxEnergy();
            int newEnergy = Math.min(currentEnergy + refund, maxEnergy);
            user.setCurrentEnergy(newEnergy);
            userRepository.save(user);
        }

        return CancelLessonResponse.builder()
                .energyRefunded(needRefund ? refund : 0)
                .currentEnergy(user.getCurrentEnergy())
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
     * Ghi lai tung cau tra loi cua user (neu FE co gui {@code answers}), tu xac dinh is_correct
     * bang cach doi chieu {@code selectedOptionId} voi DB (khong tin FE). Answer khong hop le
     * (option khong thuoc question, hoac question khong thuoc lesson) bi bo qua + log warning.
     *
     * <p>Optional & backward-compatible: {@code answers == null} (FE cu chua cap nhat) -> tra ve
     * danh sach rong, khong lam gi them.</p>
     *
     * @return danh sach answer da cham + luu thanh cong, dung lam dau vao cho Mistake Bank.
     */
    private List<LessonAttemptAnswer> recordAnswers(Lesson lesson, Long userId, List<AnswerItem> answers) {
        if (answers == null || answers.isEmpty()) {
            return List.of();
        }

        List<LessonAttemptAnswer> saved = new ArrayList<>(answers.size());
        for (AnswerItem item : answers) {
            if (item.getQuestionId() == null || item.getSelectedOptionId() == null) {
                log.warn("Bo qua answer thieu questionId/selectedOptionId: user={} lesson={}",
                        userId, lesson.getId());
                continue;
            }

            LessonQuestionOption option = optionRepository.findById(item.getSelectedOptionId()).orElse(null);
            if (option == null || !Objects.equals(option.getQuestionId(), item.getQuestionId())) {
                log.warn("Bo qua answer khong hop le (option khong thuoc question): user={} lesson={} "
                                + "questionId={} selectedOptionId={}",
                        userId, lesson.getId(), item.getQuestionId(), item.getSelectedOptionId());
                continue;
            }

            LessonQuestion question = questionRepository.findById(item.getQuestionId()).orElse(null);
            if (question == null || !Objects.equals(question.getLessonId(), lesson.getId())) {
                log.warn("Bo qua answer khong hop le (question khong thuoc lesson): user={} lesson={} questionId={}",
                        userId, lesson.getId(), item.getQuestionId());
                continue;
            }

            saved.add(lessonAttemptAnswerRepository.save(LessonAttemptAnswer.builder()
                    .userId(userId)
                    .lessonId(lesson.getId())
                    .questionId(item.getQuestionId())
                    .selectedOptionId(item.getSelectedOptionId())
                    .isCorrect(Boolean.TRUE.equals(option.getCorrect()))
                    .answeredAt(LocalDateTime.now())
                    .build()));
        }
        return saved;
    }

    /**
     * Tinh entry cost tu configJson.
     * <ul>
     *   <li>Neu configJson.co {@code entryCostEnergy} -> dung no (uu tien).</li>
     *   <li>Neu khong -> dung {@link #DEFAULT_ENTRY_COST_ENERGY} (10, khop voi
     *       {@code max_energy} mac dinh 25 - xem {@code EnergyService.MAX_ENERGY}).</li>
     * </ul>
     *
     * <p>Tach rieng hang so nay khoi {@code questionsPerSession} de 2 khai niem doc lap:
     * 1 lesson van co the co 10 cau nhung chi phi nang luong de vao hoc la con so rieng.
     * Bai lam tot se duoc hoan bot luc submit (xem {@link #PERFECT_LESSON_ENERGY_REFUND}),
     * nen chi phi thuc te thap hon 10 neu lam tot.</p>
     */
    private int resolveEntryCost(Lesson lesson) {
        JsonNode config = lesson.getConfigJson();
        if (config != null && config.has("entryCostEnergy")) {
            return config.get("entryCostEnergy").asInt();
        }
        return DEFAULT_ENTRY_COST_ENERGY;
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
    private Rank resolveHighestQualifyingRank(Integer userExp) {
        if (userExp == null) {
            userExp = 0;
        }
        return rankRepository.findFirstByMinExpRequiredLessThanEqualOrderByOrderIndexDesc(userExp)
                .orElse(null);
    }

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

    private int calculateStreakBonus(Integer currentStreak) {
        return (currentStreak != null && currentStreak >= 7) ? 5 : 0;
    }
}