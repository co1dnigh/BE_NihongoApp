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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ghi nhận 1 ngày user đã học — bảng {@code user_streak_days}.
 *
 * <p>Dùng vẽ heat map trên streak calendar (Duolingo-style): mỗi ô = 1 ngày,
 * tô màu theo số lần học/timestamp. Dữ liệu được ghi mỗi lần user hoàn thành
 * 1 bài học trong ngày (idempotent theo user_id + study_date).</p>
 */
@Entity
@Table(name = "user_streak_days")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreakDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Liên kết ngược về User (lazy, chỉ dùng để hydrate khi cần). */
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_user_streak_day_user"))
    private User user;
}