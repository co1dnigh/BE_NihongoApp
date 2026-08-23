package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Thành tích của 1 user — bảng {@code user_achievements}.
 *
 * <p>1 user có thể unlock nhiều achievement. {@code progress} là tiến độ hiện tại
 * đối với achievement có threshold > 1 (vd COIN_MASTER cần 1000 coin, progress
 * = số coin đã tích lũy). Khi {@code progress >= threshold} và {@code unlockedAt}
 * còn null → coi là vừa unlock.</p>
 */
@Entity
@Table(name = "user_achievements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "achievement_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_achievement_achievement"))
    private Achievement achievement;

    /** Thời điểm unlock (null = chưa unlock, chỉ có progress). */
    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    /** Tiến độ hiện tại so với threshold. */
    @Column(name = "progress", nullable = false)
    private Integer progress;
}