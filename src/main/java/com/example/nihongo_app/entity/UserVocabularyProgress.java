package com.example.nihongo_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trạng thái ôn tập ngắt quãng của MỘT người học với MỘT từ (thuật toán SM-2).
 *
 * <p>Khoá chính là cặp (user, vocabulary) — xem {@link UserVocabularyProgressId}.</p>
 *
 * <p>{@code nextDueAt} là trái tim của tính năng: nó trả lời câu "hôm nay cần ôn gì".
 * {@code firstLearnedAt} phục vụ hai việc khác: dựng sổ tay "từ đã học", và biết một
 * từ có phải TỪ MỚI hay không để gắn nhãn trên màn hỏi.</p>
 */
@Entity
@Table(name = "user_vocabulary_progress")
@IdClass(UserVocabularyProgressId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVocabularyProgress {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    /** Số lần trả lời đúng LIÊN TIẾP. Sai một lần là về 0. */
    @Column(name = "repetitions", nullable = false)
    private Integer repetitions;

    /** Hệ số "dễ nhớ" của SM-2, khởi tạo 2.5, sàn 1.3. */
    @Column(name = "ease_factor", nullable = false, precision = 4, scale = 3)
    private BigDecimal easeFactor;

    /**
     * Khoảng cách tới lần ôn kế tiếp, tính bằng PHÚT.
     *
     * <p>SM-2 gốc tính bằng ngày, nhưng với app di động thì từ vừa học xong mà hẹn
     * "mai gặp lại" là quá muộn — người học rời phiên và quên sạch. Dùng phút cho
     * phép có bước học ngắn (10 phút) để từ mới quay lại ngay trong cùng phiên.</p>
     */
    @Column(name = "interval_minutes", nullable = false)
    private Integer intervalMinutes;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    /** {@code null} = chưa từng học, không nằm trong hàng đợi ôn. */
    @Column(name = "next_due_at")
    private LocalDateTime nextDueAt;

    @Column(name = "first_learned_at")
    private LocalDateTime firstLearnedAt;

    @Column(name = "total_correct", nullable = false)
    private Integer totalCorrect;

    @Column(name = "total_wrong", nullable = false)
    private Integer totalWrong;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
