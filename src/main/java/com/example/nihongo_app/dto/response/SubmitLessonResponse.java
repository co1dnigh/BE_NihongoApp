package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Response cho {@code POST /api/v1/lessons/{id}/submit}.
 *
 * <p>Trả về thông tin để frontend hiển thị màn hình "Chúc mừng":
 * trạng thái hoàn thành, EXP nhận được, số sao (nếu có), cờ đánh dấu
 * đã hoàn thành cả topic hay chưa, kèm message ngắn để hiển thị.</p>
 */
@Value
@Builder
public class SubmitLessonResponse {

    /**
     * Trạng thái mới của bài học trong hệ thống.
     * Thường là {@code COMPLETED}, nhưng có thể giữ {@code IN_PROGRESS} nếu user
     * nộp bài nhưng chưa đạt điều kiện (hiện tại không xảy ra ở MVP nhưng để sẵn).
     */
    String status;

    /** EXP mà user vừa nhận được từ lần nộp bài này (đã được cộng vào {@code users.exp}). */
    Integer expEarned;

    /** Coin mà user vừa nhận được từ lần nộp bài này (đã được cộng vào {@code users.coins}). */
    Integer coinsEarned;

    /**
     * Số sao đạt được (chỉ có ý nghĩa với TIMED_REVIEW).
     * Các loại bài khác luôn là 0.
     */
    Integer starsEarned;

    /**
     * {@code true} nếu sau khi hoàn thành bài này, toàn bộ NORMAL + TIMED_REVIEW
     * trong Topic đã xong. Dùng cho JUMP_TEST nhảy cóc (mark tất cả bài NORMAL
     * trong topic là COMPLETED → isTopicCompleted = true) hoặc cho bài cuối
     * của topic.
     */
    Boolean isTopicCompleted;

    /**
     * Message hiển thị trên UI ("Tuyệt vời, bạn đã hoàn thành bài học!").
     * FE có thể override tuỳ ngữ cảnh.
     */
    String message;

    /**
     * Năng lượng hiện tại của user sau khi cộng thưởng / trừ phí.
     * FE dùng để cập nhật thanh năng lượng mà không cần gọi thêm /me.
     */
    Integer currentEnergy;
}