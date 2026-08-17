package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Ghi lại 1 câu trả lời cụ thể của user trong 1 lượt làm bài (start/submit), kèm
 * is_correct đã được server tự xác định (đối chiếu {@code selectedOptionId} với
 * {@link LessonQuestionOption}, không nhận is_correct từ FE). Là nguồn dữ liệu đầu vào
 * cho Mistake Bank ({@link Mistake}).
 */
@Entity
@Table(name = "lesson_attempt_answers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "selected_option_id", nullable = false)
    private Long selectedOptionId;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
}
