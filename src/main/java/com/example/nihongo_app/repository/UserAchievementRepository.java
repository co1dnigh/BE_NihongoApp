package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.UserAchievement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Trạng thái achievement của user: đã unlock hay chưa, tiến độ hiện tại.
 */
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    /** Lấy achievement của user theo achievement_id (dùng check-on-event). */
    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);

    /** Lấy achievement của user theo achievement code. */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId "
            + "AND ua.achievement.code = :code")
    Optional<UserAchievement> findByUserIdAndAchievementCode(
            @Param("userId") Long userId, @Param("code") String code);

    /** Danh sách achievement user đã unlock (có unlockedAt != null). */
    List<UserAchievement> findAllByUserIdAndUnlockedAtIsNotNull(Long userId);

    /** Danh sách achievement user đã có record (unlock hoặc đang progress). */
    List<UserAchievement> findAllByUserId(Long userId);
}