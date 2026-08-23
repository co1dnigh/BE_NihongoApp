package com.example.nihongo_app.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

/**
 * Một "sự kiện" emit từ service layer → {@link AchievementService#onEvent}.
 *
 * <p>Loại {@link AchievementProgress.EventType} là chuỗi để so sánh với
 * {@code achievements.type} trong DB (linh hoạt, không cần enum mapping).
 * {@code value} là giá trị tuyệt đối user đã đạt được (vd streak=7, lessons=10,
 * coin=1500). {@code metadata} là thông tin bổ sung (vd topicId, studyHour).
 */
@Value
@lombok.Builder
public class AchievementProgress {

    @NotBlank
    String type;

    /** Giá trị tuyệt đối user đã tích lũy được (so sánh với threshold). */
    int value;

    /** Thông tin bổ sung (tùy loại, nullable). */
    String metadata;

    public static AchievementProgress of(String type, int value) {
        return AchievementProgress.builder().type(type).value(value).build();
    }

    public static AchievementProgress of(String type, int value, String metadata) {
        return AchievementProgress.builder().type(type).value(value).metadata(metadata).build();
    }

    public enum EventType {
        LESSONS_COMPLETED,
        STREAK_MILESTONE,
        PERFECT_LESSON,
        TOPIC_COMPLETED,
        COIN_EARNED,
        STREAK_FREEZE_USED,
        DAILY_STUDY,
        STREAK_RESET,
    }
}