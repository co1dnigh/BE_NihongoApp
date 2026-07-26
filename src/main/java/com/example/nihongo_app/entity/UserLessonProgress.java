package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lịch sử học của từng User theo từng Lesson.
 * Bảng vật lý: {@code user_lesson_progress} (tạo bởi V3__create_user_progress_tables.sql,
 * bổ sung cột {@code stars_earned} bởi V10__update_core_learning_structure.sql).
 *
 * <p>Enum {@link ProgressStatus} phản ánh trạng thái do hệ thống ghi, không nhất thiết
 * khớp 1-1 với trạng thái hiển thị trên bản đồ. Trạng thái hiển thị (LOCKED / UNLOCKED /
 * COMPLETED) được tính toán lại ở tầng service dựa trên lộ trình tổng thể.</p>
 */
@Entity
@Table(name = "user_lesson_progress")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLessonProgress {

    public enum ProgressStatus {
        LOCKED, IN_PROGRESS, COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgressStatus status;

    @Column(name = "stars_earned", nullable = false)
    private Integer starsEarned;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
