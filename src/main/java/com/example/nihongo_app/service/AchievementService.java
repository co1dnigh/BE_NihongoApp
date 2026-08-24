package com.example.nihongo_app.service;

import com.example.nihongo_app.entity.Achievement;
import com.example.nihongo_app.entity.UserAchievement;
import java.util.List;

/**
 * Hệ thống thành tích (Achievements / Huy chương).
 *
 * <p>Thiết kế sự kiện-driven: service layer emit {@link AchievementProgress} khi
 * user làm gì đó (hoàn bài, đạt streak, học vào giờ particular...).
 * {@link #onEvent} upsert progress + unlock achievement nếu {@code value >= threshold}.
 * Secret achievement ({@code secret=true}) bị ẩn khỏi danh sách cho tới khi unlock.</p>
 */
public interface AchievementService {

    /**
     * Xử lý 1 sự kiện của user. Upsert {@link UserAchievement} (tạo row progress=0 nếu
     * chưa có), cập nhật progress = max(progress, value). Nếu value >= threshold và
     * chưa unlock → set unlockedAt = now, coi là unlock.
     *
     * <p>Idempotent: gọi nhiều lần với cùng value không unlock lại.</p>
     */
    void onEvent(Long userId, AchievementProgress progress);

    /** Danh sách achievement đang active, kèm trạng thái của user (unlock/progress). */
    List<Achievement> listAchievements(Long userId);

    /** Danh sách achievement user đã unlock. */
    List<UserAchievement> getUnlocked(Long userId);

    /** Danh sách achievement user đang progress (chưa unlock). */
    List<UserAchievement> getProgress(Long userId);
}