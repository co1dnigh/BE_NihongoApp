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
 * 1 câu hỏi mà user đang (hoặc từng) sai — "Mistake Bank". Mỗi cặp (user, question) chỉ có
 * đúng 1 row (unique), cập nhật dồn wrong_count/correct_streak/status theo thời gian thay vì
 * tạo row mới mỗi lần sai.
 *
 * <p>{@code status = ACTIVE}: còn cần ôn lại. {@code status = RESOLVED}: đã "xoá nợ" — trả lời
 * đúng {@code correct-streak} lần liên tiếp (mặc định 2), tính từ những lần đúng cách nhau
 * tối thiểu {@code resolve-min-gap-days} ngày (mặc định 1) để chống học vẹt đáp án trong
 * cùng 1 phiên ôn. Sai lại bất kỳ lúc nào (kể cả khi đã RESOLVED) sẽ mở lại ACTIVE.</p>
 */
@Entity
@Table(name = "user_mistakes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mistake {

    public enum Status {
        ACTIVE, RESOLVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount;

    @Column(name = "correct_streak", nullable = false)
    private Integer correctStreak;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "last_wrong_at", nullable = false)
    private LocalDateTime lastWrongAt;

    @Column(name = "last_correct_at")
    private LocalDateTime lastCorrectAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
