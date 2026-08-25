package com.example.nihongo_app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Request cho {@code POST /api/v1/lessons/{id}/submit}.
 *
 * <p>Body này được thiết kế "tổng hợp" để chỉ cần 1 endpoint duy nhất cho cả các dạng bài
 * (NORMAL / TOPIC_REVIEW / JUMP_TEST / TIMED_REVIEW). Field nào không dùng cho bài hiện tại sẽ được
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

    /** Bắt buộc cho TOPIC_REVIEW (tính sao). Có thể null với NORMAL / JUMP_TEST. */
    private Integer timeTakenSeconds;

    /** Bắt buộc cho JUMP_TEST. Có thể null với NORMAL / TOPIC_REVIEW. */
    private Integer heartsRemaining;

    /**
     * FE set = true khi bài này là replay (đã lấy từ {@code StartLessonResponse.isReplay}).
     * BE dùng cờ này để giảm EXP (xem {@code LessonAttemptServiceImpl.resolveReplayExpRatio}).
     * Nếu FE quên gửi hoặc gửi sai, BE sẽ tự suy ra lại từ DB.
     */
    private Boolean isReplay;

    /**
     * OPTIONAL — danh sách câu trả lời chi tiết (câu nào, chọn option nào), để BE tự chấm
     * và ghi vào {@code lesson_attempt_answers} + Mistake Bank. Field cũ, không có field này
     * (FE chưa cập nhật) vẫn hoạt động y nguyên như trước — chỉ bỏ qua phần ghi answer/mistake.
     *
     * <p>Trước đây field này là {@code List<AnswerDto>} với {@code isCorrect} do FE tự gửi —
     * đã đổi sang {@link AnswerItem} (chỉ gửi lựa chọn, BE tự tra DB để xác định đúng/sai)
     * vì field cũ chưa từng được service đọc tới (dead code) và đúng dạng anti-pattern
     * (tin FE) mà Mistake Bank cần tránh.</p>
     */
    private List<AnswerItem> answers;
}