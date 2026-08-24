package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

public enum AuthProvider {
    LOCAL,
    GOOGLE,
    FACEBOOK
}

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@EqualsAndHashCode.Include
private Long id;

@Column(nullable = false, unique = true, length = 255)
private String email;

@Column(unique = true, length = 50)
private String username;

@Column(name = "phone_number", unique = true)
private String phoneNumber;

@Column(name = "password_hash", length = 255)
private String passwordHash;

@Enumerated(EnumType.STRING)
@Column(name = "auth_provider", nullable = false, length = 20)
private AuthProvider authProvider;

@Column(name = "provider_id", length = 255)
private String providerId;

@Column(name = "display_name", nullable = false, length = 100)
private String displayName;

@Column(name = "avatar_url", length = 255)
private String avatarUrl;

@Column(nullable = false, length = 50)
private String role;

// --- Gamification ---
@Column(name = "level")
private Integer level;

@Column(name = "exp")
private Integer exp;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "rank_id", nullable = false)
private Rank rank;

@Column(name = "current_energy")
private Integer currentEnergy;

@Column(name = "max_energy")
private Integer maxEnergy;

@Column(name = "last_streak_date")
private LocalDate lastStreakDate;

@Column(name = "streak_freeze_count")
private Integer streakFreezeCount;

@Column(name = "streak_freeze_awarded")
private Boolean streakFreezeAwarded;

/** Cài đặt: có hiển thị streak calendar (heat map) trên FE không. */
@Column(name = "streak_calendar_enabled")
private Boolean streakCalendarEnabled;

@Column(name = "last_energy_reset_date")
private LocalDateTime lastEnergyResetDate;

@Column(name = "last_learning_at")
private LocalDateTime lastLearningAt;

@Column(name = "last_rank_decay_at")
private LocalDateTime lastRankDecayAt;

@Column(name = "last_rank_reminder_at")
private LocalDateTime lastRankReminderAt;

@Column(name = "last_ad_watch_date")
private LocalDateTime lastAdWatchDate;

@Column(name = "last_chest_opened_date")
private LocalDate lastChestOpenedDate;

// --- Mistake Review (dem so phien on da duoc thuong nang luong trong ngay) ---
@Column(name = "last_mistake_review_reward_date")
private LocalDate lastMistakeReviewRewardDate;

@Column(name = "mistake_review_reward_count_today")
private Integer mistakeReviewRewardCountToday;

@Column(name = "coins")
private Integer coins;

@Column(name = "current_streak")
private Integer currentStreak;

@Column(name = "longest_streak")
private Integer longestStreak;

@Column(name = "created_at", insertable = false, updatable = false)
private LocalDateTime createdAt;

@Column(name = "updated_at", insertable = false, updatable = false)
private LocalDateTime updatedAt;

@Column(name = "deleted_at")
private LocalDateTime deletedAt;

@PrePersist
void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    if (createdAt == null) {
        createdAt = now;
    }
    if (updatedAt == null) {
        updatedAt = now;
    }

    if ("LEARNER".equals(role)) {
        if (authProvider == null) authProvider = AuthProvider.LOCAL;
        if (level == null) level = 1;
        if (exp == null) exp = 0;
        if (rank == null) {
            rank = Rank.builder().id(1L).name("BRONZE").minExpRequired(0).orderIndex(1).build();
        }
        if (currentEnergy == null) currentEnergy = 25;
        if (maxEnergy == null) maxEnergy = 25;
        if (coins == null) coins = 0;
        if (currentStreak == null) currentStreak = 0;
        if (longestStreak == null) longestStreak = 0;
        if (streakFreezeCount == null) streakFreezeCount = 0;
        if (streakFreezeAwarded == null) streakFreezeAwarded = false;
        if (streakCalendarEnabled == null) streakCalendarEnabled = true;
        if (mistakeReviewRewardCountToday == null) mistakeReviewRewardCountToday = 0;
    }
}

@PreUpdate
void onUpdate() {
    updatedAt = LocalDateTime.now();
}
}
