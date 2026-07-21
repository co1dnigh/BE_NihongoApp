package com.nihongoapp.lesson.service;

import com.nihongoapp.common.exception.BusinessException;
import com.nihongoapp.gamification.model.LevelConfig;
import com.nihongoapp.gamification.model.OutboxEvent;
import com.nihongoapp.gamification.model.LessonCompletionLog;
import com.nihongoapp.gamification.model.LessonCompletionLogId;
import com.nihongoapp.gamification.repository.LevelConfigRepository;
import com.nihongoapp.gamification.repository.OutboxEventRepository;
import com.nihongoapp.gamification.repository.LessonCompletionLogRepository;
import com.nihongoapp.user.model.User;
import com.nihongoapp.user.repository.UserRepository;
import com.nihongoapp.wallet.model.WalletTransactionReason;
import com.nihongoapp.wallet.dto.WalletBalanceResponse;
import com.nihongoapp.wallet.model.UserInventory;
import com.nihongoapp.wallet.repository.UserInventoryRepository;
import com.nihongoapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class LessonService {

    private static final String IDEM_KEY_PREFIX = "idem:lesson:";
    private static final int XP_PER_CORRECT = 10;
    private static final int GEM_PER_CORRECT = 3;
    private static final int IDEM_TTL_SECONDS = 600;

    private final UserRepository userRepo;
    private final WalletService walletService;
    private final LessonCompletionLogRepository logRepo;
    private final LevelConfigRepository levelRepo;
    private final OutboxEventRepository outboxRepo;
    private final UserInventoryRepository inventoryRepo;
    private final RedisTemplate<String, String> redisStringTemplate;

    public LessonService(
            UserRepository userRepo,
            WalletService walletService,
            LessonCompletionLogRepository logRepo,
            LevelConfigRepository levelRepo,
            OutboxEventRepository outboxRepo,
            UserInventoryRepository inventoryRepo,
            @Qualifier("redisStringTemplate") RedisTemplate<String, String> redisStringTemplate) {
        this.userRepo = userRepo;
        this.walletService = walletService;
        this.logRepo = logRepo;
        this.levelRepo = levelRepo;
        this.outboxRepo = outboxRepo;
        this.inventoryRepo = inventoryRepo;
        this.redisStringTemplate = redisStringTemplate;
    }

    @Transactional
    public LessonCompleteResult completeLesson(Long userId, Long lessonId,
                                               List<ExerciseResult> exerciseResults,
                                               String idempotencyKey) {
        String cacheKey = "cache:lesson:" + idempotencyKey;

        String cached = redisStringTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return deserializeResult(cached);
        }

        String redisKey = IDEM_KEY_PREFIX + idempotencyKey;
        Boolean set = redisStringTemplate.opsForValue()
                .setIfAbsent(redisKey, "processing", IDEM_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(set)) {
            throw new BusinessException("CONFLICT", "Request already being processed");
        }

        try {
            return doComplete(userId, lessonId, exerciseResults, idempotencyKey, cacheKey);
        } finally {
            redisStringTemplate.delete(redisKey);
        }
    }

    private LessonCompleteResult doComplete(Long userId, Long lessonId,
                                            List<ExerciseResult> exerciseResults,
                                            String idempotencyKey,
                                            String cacheKey) {

        LessonCompletionLogId logId = new LessonCompletionLogId(userId, lessonId);
        if (logRepo.existsById(logId)) {
            throw new BusinessException("CONFLICT", "Lesson already completed");
        }

        User user = userRepo.findById(userId).orElseThrow();

        int correctCount = 0;
        boolean outOfHearts = false;

        for (ExerciseResult ex : exerciseResults) {
            if (user.getHearts() <= 0) {
                outOfHearts = true;
                break;
            }
            if (!ex.isCorrect()) {
                user.setHearts(user.getHearts() - 1);
                user.setLastHeartLostAt(java.time.LocalDateTime.now());
            } else {
                correctCount++;
            }
        }

        if (outOfHearts) {
            user.setHearts(0);
            userRepo.save(user);
            LessonCompleteResult r = new LessonCompleteResult(
                    false, 0, 0, user.getCurrentLevel(),
                    user.getHearts(), user.getCurrentStreak(), null);
            cacheResult(cacheKey, r);
            return r;
        }

        int xpEarned = correctCount * XP_PER_CORRECT;
        int gemEarned = correctCount * GEM_PER_CORRECT;

        int newTotalXp = user.getTotalXp() + xpEarned;
        Integer newLevel = calculateLevel(newTotalXp);
        int newStreak = updateStreak(user);

        walletService.addCurrency(userId, (long) gemEarned, WalletTransactionReason.LESSON_REWARD,
                "reward:" + userId + ":" + lessonId);

        user.setTotalXp(newTotalXp);
        user.setCurrentLevel(newLevel);
        User saved = userRepo.save(user);

        LessonCompletionLog log = new LessonCompletionLog();
        log.setId(logId);
        log.setCompletedAt(java.time.LocalDateTime.now());
        logRepo.save(log);

        OutboxEvent outbox = new OutboxEvent();
        outbox.setEventType("LEADERBOARD_UPDATE");
        outbox.setPayload(buildOutboxPayload(userId, saved.getLeagueId(), xpEarned));
        outbox.setStatus(OutboxEvent.Status.pending);
        outbox.setRetryCount(0);
        outbox.setCreatedAt(java.time.LocalDateTime.now());
        outboxRepo.save(outbox);

        WalletBalanceResponse bal = walletService.getBalance(userId);
        LessonCompleteResult result = new LessonCompleteResult(
                true, xpEarned, gemEarned, newLevel,
                saved.getHearts(), newStreak, bal.gem());
        cacheResult(cacheKey, result);
        return result;
    }

    private int updateStreak(User user) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate lastDate = user.getLastLessonCompletedDate();
        int streak = user.getCurrentStreak() != null ? user.getCurrentStreak() : 0;

        if (lastDate == null) {
            streak = 1;
        } else if (lastDate.equals(today)) {
            return streak;
        } else if (lastDate.plusDays(1).equals(today)) {
            streak++;
        } else if (lastDate.plusDays(2).equals(today)) {
            Optional<UserInventory> freeze = inventoryRepo.findByUserIdAndItemType(
                    user.getId(), UserInventory.ItemType.STREAK_FREEZE);
            if (freeze.isPresent() && freeze.get().getQuantity() > 0) {
                inventoryRepo.insertOrAddQuantity(user.getId(), "STREAK_FREEZE", null, -1);
                return streak;
            }
            streak = 1;
        } else {
            streak = 1;
        }

        user.setCurrentStreak(streak);
        user.setLastLessonCompletedDate(today);
        return streak;
    }

    private Integer calculateLevel(int totalXp) {
        List<LevelConfig> configs = levelRepo.findAll();
        int level = 1;
        for (LevelConfig cfg : configs) {
            if (totalXp >= cfg.getXpRequired()) {
                level = cfg.getLevel();
            } else {
                break;
            }
        }
        return level;
    }

    private String buildOutboxPayload(Long userId, Long leagueId, int xpEarned) {
        String league = leagueId != null ? leagueId.toString() : "default";
        return String.format("{\"user_id\":%d,\"league_id\":\"%s\",\"xp_earned\":%d}",
                userId, league, xpEarned);
    }

    private void cacheResult(String cacheKey, LessonCompleteResult r) {
        try {
            String serialized = serializeResult(r);
            redisStringTemplate.opsForValue().set(cacheKey, serialized, IDEM_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Redis cache failure is non-critical
        }
    }

    private String serializeResult(LessonCompleteResult r) {
        return String.join("|",
                String.valueOf(r.lessonCompleted()),
                String.valueOf(r.xpEarned()),
                String.valueOf(r.gemEarned()),
                String.valueOf(r.newLevel()),
                String.valueOf(r.heartsRemaining()),
                String.valueOf(r.newStreak()),
                r.gemBalance() != null ? String.valueOf(r.gemBalance()) : "null"
        );
    }

    private LessonCompleteResult deserializeResult(String s) {
        String[] p = s.split("\\|");
        Long gem = "null".equals(p[7]) ? null : Long.parseLong(p[7]);
        return new LessonCompleteResult(
                Boolean.parseBoolean(p[0]),
                Integer.parseInt(p[1]),
                Integer.parseInt(p[2]),
                Integer.parseInt(p[3]),
                Integer.parseInt(p[4]),
                Integer.parseInt(p[5]),
                gem
        );
    }

    public record LessonCompleteResult(
            boolean lessonCompleted,
            int xpEarned,
            int gemEarned,
            int newLevel,
            int heartsRemaining,
            int newStreak,
            Long gemBalance
    ) {}

    public record ExerciseResult(
            Long questionId,
            boolean isCorrect
    ) {}
}
