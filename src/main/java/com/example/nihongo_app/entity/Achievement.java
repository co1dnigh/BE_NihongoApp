package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Định nghĩa một thành tích (huy chương) — bảng {@code achievements}.
 *
 * <p>Mỗi achievement có 1 {@code code} duy nhất dùng làm định danh trong code
 * ({@code AchievementProgress} event). {@code threshold} là giá trị sự kiện cần
 * đạt để unlock (vd streak=7, lessons=1). {@code secret=true} → achievement
 * ẩn khỏi danh sách cho tới khi user thực sự unlock (giúp khám phá).</p>
 */
@Entity
@Table(name = "achievements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 100)
    private String icon;

    /** Loại sự kiện: LESSONS_COMPLETED, STREAK_MILESTONE, PERFECT_LESSON, ... */
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    /** Giá trị sự kiện cần đạt để unlock. */
    @Column(name = "threshold", nullable = false)
    private Integer threshold;

    /** true → achievement ẩn, chỉ hiện sau khi unlock. */
    @Column(name = "secret", nullable = false)
    private Boolean secret;

    @Column(nullable = false)
    private Boolean active;
}