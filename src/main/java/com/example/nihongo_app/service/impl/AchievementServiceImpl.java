package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.entity.Achievement;
import com.example.nihongo_app.entity.UserAchievement;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.AchievementRepository;
import com.example.nihongo_app.repository.UserAchievementRepository;
import com.example.nihongo_app.service.AchievementProgress;
import com.example.nihongo_app.service.AchievementService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation của {@link AchievementService}.
 *
 * <p>Chế độ {@code onEvent}: với mỗi event, upsert progress theo achievement type.
 * Có 1 số event đặc biệt cần metadata:
 * <ul>
 *   <li>{@code DAILY_STUDY}: metadata = hour of day (0-23). EARLY_BIRD nếu &lt; 9,
 *       NIGHT_OWL nếu &gt;= 22.</li>
 *   <li>{@code STREAK_RESET}: metadata = previous streak length (int). COMEBACK
 *       unlock nếu previous streak &gt;= threshold (7).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    @Override
    @Transactional
    public void onEvent(Long userId, AchievementProgress progress) {
        if (progress == null || progress.getType() == null || progress.getType().isBlank()) {
            return;
        }
        if (userId == null) {
            return;
        }

        String type = progress.getType();
        int value = Math.max(0, progress.getValue());

        switch (type) {
            case "DAILY_STUDY" -> handleDailyStudy(userId, progress.getMetadata());
            case "STREAK_RESET" -> handleStreakReset(userId, progress.getMetadata());
            default -> handleGeneric(userId, type, value);
        }
    }

    /** Duyệt tất cả achievement đang active có type == type, upsert progress + unlock. */
    private void handleGeneric(Long userId, String type, int value) {
        List<Achievement> candidates = achievementRepository.findAllByActiveTrue().stream()
                .filter(a -> Objects.equals(a.getType(), type))
                .toList();

        for (Achievement achievement : candidates) {
            upsertProgress(userId, achievement, value);
        }
    }

    /**
     * EARLY_BIRD (học trước 9h) / NIGHT_OWL (học sau 22h).
     * metadata = hour of day (0-23) dưới dạng string.
     */
    private void handleDailyStudy(Long userId, String metadata) {
        int hour;
        try {
            hour = Integer.parseInt(metadata == null ? "-1" : metadata.trim());
        } catch (NumberFormatException ex) {
            log.warn("DAILY_STUDY metadata không phải số: {}", metadata);
            return;
        }

        List<Achievement> candidates = achievementRepository.findAllByActiveTrue().stream()
                .filter(a -> Objects.equals(a.getType(), "DAILY_STUDY"))
                .filter(a -> matchesDailyStudy(a, hour))
                .toList();

        for (Achievement achievement : candidates) {
            upsertProgress(userId, achievement, 1);
        }
    }

    private boolean matchesDailyStudy(Achievement a, int hour) {
        return switch (a.getCode()) {
            case "EARLY_BIRD" -> hour >= 0 && hour < 9;
            case "NIGHT_OWL" -> hour >= 22 && hour <= 23;
            default -> false;
        };
    }

    /**
     * COMEBACK: unlock khi streak vừa reset và streak trước đó >= threshold (7).
     * metadata = previous streak length (int).
     */
    private void handleStreakReset(Long userId, String metadata) {
        int previousStreak;
        try {
            previousStreak = Integer.parseInt(metadata == null ? "0" : metadata.trim());
        } catch (NumberFormatException ex) {
            log.warn("STREAK_RESET metadata không phải số: {}", metadata);
            return;
        }

        achievementRepository.findByCodeAndActiveTrue("COMEBACK")
                .ifPresent(achievement -> {
                    if (previousStreak >= achievement.getThreshold()) {
                        upsertProgress(userId, achievement, previousStreak);
                    }
                });
    }

    /**
     * Upsert progress: nếu user chưa có row → tạo progress=value; đã có →
     * progress = max(progress, value). Unlock nếu value >= threshold và
     * unlockedAt == null.
     */
    private void upsertProgress(Long userId, Achievement achievement, int value) {
        if (value < achievement.getThreshold()) {
            // Chưa đủ điều kiện unlock. Chỉ cập nhật progress in-memory để FE
            // hiển thị thanh tiến độ; không save (không có row nào để update).
            userAchievementRepository.findByUserIdAndAchievementId(userId, achievement.getId())
                    .ifPresent(ua -> ua.setProgress(Math.max(ua.getProgress(), value)));
            return;
        }

        // Đủ điều kiện unlock.
        userAchievementRepository.findByUserIdAndAchievementId(userId, achievement.getId())
                .map(ua -> {
                    if (ua.getUnlockedAt() == null) {
                        ua.setUnlockedAt(LocalDateTime.now());
                    }
                    ua.setProgress(Math.max(ua.getProgress(), value));
                    return ua;
                })
                .orElseGet(() -> userAchievementRepository.save(UserAchievement.builder()
                        .userId(userId)
                        .achievement(achievement)
                        .unlockedAt(LocalDateTime.now())
                        .progress(value)
                        .build()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Achievement> listAchievements(Long userId) {
        List<Achievement> all = achievementRepository.findAllByActiveTrue();
        Map<Long, UserAchievement> unlockedById = new HashMap<>();
        Map<Long, UserAchievement> progressById = new HashMap<>();
        for (UserAchievement ua : userAchievementRepository.findAllByUserId(userId)) {
            if (ua.getUnlockedAt() != null) {
                unlockedById.put(ua.getAchievement().getId(), ua);
            } else {
                progressById.put(ua.getAchievement().getId(), ua);
            }
        }

        List<Achievement> result = new ArrayList<>(all.size());
        for (Achievement a : all) {
            // Ẩn secret achievement chưa unlock khỏi danh sách.
            if (Boolean.TRUE.equals(a.getSecret())
                    && !unlockedById.containsKey(a.getId())) {
                continue;
            }
            result.add(a);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievement> getUnlocked(Long userId) {
        return userAchievementRepository.findAllByUserIdAndUnlockedAtIsNotNull(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievement> getProgress(Long userId) {
        return userAchievementRepository.findAllByUserId(userId).stream()
                .filter(ua -> ua.getUnlockedAt() == null)
                .toList();
    }
}