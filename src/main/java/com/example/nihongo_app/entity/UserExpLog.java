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
 * Log ghi nhận EXP mà user nhận được từ việc hoàn thành bài học.
 * Bảng vật lý: {@code user_exp_logs} (tạo bởi V11__update_gamification_and_energy.sql).
 *
 * <p>Dùng cho:
 * <ul>
 *   <li>Tính tổng EXP trong tuần → Weekly Leaderboard.</li>
 *   <li>Đối chiếu khi user khiếu nại về điểm/EXP.</li>
 * </ul>
 *
 * <p>Mỗi lần {@code POST /api/v1/lessons/{id}/submit} thành công sẽ tạo 1 row
 * (hoặc nhiều row nếu sau này mở rộng nguồn EXP).</p>
 */
@Entity
@Table(name = "user_exp_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExpLog {

    /**
     * Phân loại nguồn EXP, tương ứng cột {@code source_type} trong DB:
     * <ul>
     *   <li>{@code NEW_LESSON}    → hoàn thành bài NORMAL.</li>
     *   <li>{@code REVIEW_LESSON} → hoàn thành bài TIMED_REVIEW (Con cú).</li>
     *   <li>{@code JUMP_TEST}     → hoàn thành bài JUMP_TEST.</li>
     * </ul>
     */
    public enum SourceType {
        NEW_LESSON, REVIEW_LESSON, JUMP_TEST
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exp_gained", nullable = false)
    private Integer expGained;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    /**
     * ID tham chiếu tuỳ ngữ cảnh:
     * <ul>
     *   <li>{@code NEW_LESSON} / {@code REVIEW_LESSON} → {@code lesson_id}.</li>
     *   <li>{@code JUMP_TEST}                          → {@code topic_id}.</li>
     * </ul>
     */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}