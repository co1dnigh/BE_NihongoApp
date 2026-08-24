package com.example.nihongo_app.service;

import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.RankRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankMaintenanceService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final EmailService emailService;

    @Value("${app.rank.inactivity-days:7}")
    private int inactivityDays;

    @Value("${app.rank.decay-exp:100}")
    private int decayExp;

    @Value("${app.rank.reminder-enabled:false}")
    private boolean reminderEnabled;

    @Scheduled(cron = "${app.rank.decay-cron:0 0 0 * * *}", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void applyWeeklyRankDecay() {
        if (inactivityDays <= 0 || decayExp < 0) {
            log.error("Rank decay skipped because configuration is invalid: inactivityDays={}, decayExp={}",
                    inactivityDays, decayExp);
            return;
        }

        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        List<User> users = userRepository.findAllByDeletedAtIsNullAndLastLearningAtIsNotNull();
        for (User user : users) {
            applyDecayIfDue(user, now);
        }
    }

    @Scheduled(cron = "${app.rank.reminder-cron:0 0 18 * * *}", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void sendRankReminders() {
        if (!reminderEnabled || inactivityDays <= 0 || decayExp < 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        LocalDateTime reminderDueAtOffset = now.minusDays(inactivityDays - 1L);
        for (User user : userRepository.findAllByDeletedAtIsNullAndLastLearningAtIsNotNull()) {
            LocalDateTime lastLearningAt = user.getLastLearningAt();
            boolean alreadySentForCurrentLearning = user.getLastRankReminderAt() != null
                    && !user.getLastRankReminderAt().isBefore(lastLearningAt);
            if (lastLearningAt.isAfter(reminderDueAtOffset) || alreadySentForCurrentLearning) {
                continue;
            }

            user.setLastRankReminderAt(now);
            userRepository.save(user);
            emailService.sendRankReminder(user.getEmail(), user.getDisplayName(), 1, decayExp);
        }
    }

    private void applyDecayIfDue(User user, LocalDateTime now) {
        LocalDateTime nextDueAt = user.getLastRankDecayAt() == null
                ? user.getLastLearningAt().plusDays(inactivityDays)
                : user.getLastRankDecayAt().plusDays(inactivityDays);
        if (nextDueAt.isAfter(now)) {
            return;
        }

        int duePeriods = 0;
        LocalDateTime lastDueAt = nextDueAt;
        while (!nextDueAt.isAfter(now)) {
            duePeriods++;
            lastDueAt = nextDueAt;
            nextDueAt = nextDueAt.plusDays(inactivityDays);
        }

        int currentExp = Objects.requireNonNullElse(user.getExp(), 0);
        long totalDecay = (long) decayExp * duePeriods;
        int updatedExp = (int) Math.max(0L, currentExp - totalDecay);
        Rank updatedRank = rankRepository
                .findFirstByMinExpRequiredLessThanEqualOrderByOrderIndexDesc(updatedExp)
                .orElse(null);

        user.setExp(updatedExp);
        user.setLastRankDecayAt(lastDueAt);
        if (updatedRank != null && (user.getRank() == null
                || !Objects.equals(user.getRank().getId(), updatedRank.getId()))) {
            user.setRank(updatedRank);
        }
        userRepository.save(user);
        log.info("Applied rank decay: userId={}, periods={}, exp={} -> {}, rank={}",
                user.getId(), duePeriods, currentExp, updatedExp,
                updatedRank == null ? null : updatedRank.getName());
    }
}