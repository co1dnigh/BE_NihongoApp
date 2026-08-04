package com.example.nihongo_app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Request cho {@code POST /api/v1/lessons/{id}/submit}.
 *
 * <p>Body này được thiết kế "tổng hợp" để chỉ cần 1 endpoint duy nhất cho cả 3 dạng bài
 * (NORMAL / TIMED_REVIEW / JUMP_TEST). Field nào không dùng cho bài hiện tại sẽ được
 * service bỏ qua (không validate quá chặt để tránh frontend phải switch-case gửi khác nhau).</p>
 *
 * <p>Các trường:</p>
 * <ul>
 *   <li>{@code totalQuestions}: tổng số câu của bài (frontend đếm được).</li>
 *   <li>{@code totalCorrect}: số câu trả lời đúng.</li>
 *   <li>{@code totalMistakes}: tổng số lần sai (bao gồm cả lần đẩy câu sai vào cuối mảng).</li>
 *   <li>{@code timeTakenSeconds}: thời gian làm bài — dùng cho TIMED_REVIEW để tính sao.</li>
 *   <li>{@code heartsRemaining}: số mạng còn lại — dùng cho JUMP_TEST (pass = heartsRemaining &gt; 0).</li>
 * </ul>
 */
@Data
public class SubmitLessonRequest {

    @NotNull
    @Min(1)
    private Integer totalQuestions;

    @NotNull
    @Min(0)
    private Integer totalCorrect;

    @NotNull
    @Min(0)
    private Integer totalMistakes;

    /** Bắt buộc cho TIMED_REVIEW. Có thể null với NORMAL / JUMP_TEST. */
    private Integer timeTakenSeconds;

    /** Bắt buộc cho JUMP_TEST. Có thể null với NORMAL / TIMED_REVIEW. */
    private Integer heartsRemaining;

    /**
     * FE set = true khi bài này là replay (đã lấy từ {@code StartLessonResponse.isReplay}).
     * BE dùng cờ này để giảm EXP (xem {@code LessonAttemptServiceImpl.resolveReplayExpRatio}).
     * Nếu FE quên gửi hoặc gửi sai, BE sẽ tự suy ra lại từ DB.
     */
    private Boolean isReplay;

    /** Danh sách câu trả lời của user, FE gửi lại toàn bộ answers để BE đếm số câu sai. */
    private List<AnswerDto> answers;

    @lombok.Data
    public static class AnswerDto {
        private Long questionId;
        private Boolean isCorrect;
    }
}