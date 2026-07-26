package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.SubmitLessonRequest;
import com.example.nihongo_app.dto.response.CancelLessonResponse;
import com.example.nihongo_app.dto.response.StartLessonResponse;
import com.example.nihongo_app.dto.response.SubmitLessonResponse;

/**
 * Service xử lý 3 API nghiệp vụ vòng đời của một lượt làm bài:
 * <ul>
 *   <li>{@link #startLesson(Long, Long)} — bắt đầu làm bài (trừ năng lượng + shuffle câu hỏi).</li>
 *   <li>{@link #submitLesson(Long, Long, SubmitLessonRequest)} — nộp bài (chấm điểm, cộng EXP, ghi log).</li>
 *   <li>{@link #cancelLesson(Long, Long)} — huỷ bài (hoàn năng lượng).</li>
 * </ul>
 *
 * <p>Service này dùng chung {@link LessonUnlockPolicy} với {@code RoadmapService}
 * để đảm bảo quyết định "bài có đang UNLOCKED không" luôn nhất quán.</p>
 */
public interface LessonAttemptService {

    /**
     * Bắt đầu làm bài.
     *
     * @param lessonId id bài học
     * @param userId   id user (lấy từ JWT)
     * @return bộ đề thi đã shuffle + số năng lượng đã trừ
     * @throws com.example.nihongo_app.exception.ResourceNotFoundException     nếu bài không tồn tại
     * @throws com.example.nihongo_app.exception.LessonLockedException         nếu bài chưa mở khoá
     * @throws com.example.nihongo_app.exception.InsufficientEnergyException   nếu user không đủ năng lượng
     */
    StartLessonResponse startLesson(Long lessonId, Long userId);

    /**
     * Nộp bài, chấm điểm theo {@code lessonType}, cộng EXP, ghi log.
     *
     * @param lessonId id bài học
     * @param userId   id user (lấy từ JWT)
     * @param request  thông tin tổng hợp từ FE (câu đúng, thời gian, mạng còn lại...)
     * @return kết quả (trạng thái, EXP, sao, cờ topic-completed, message)
     */
    SubmitLessonResponse submitLesson(Long lessonId, Long userId, SubmitLessonRequest request);

    /**
     * Huỷ bài đang làm dở, hoàn lại năng lượng đã trừ lúc start.
     * Chỉ có tác dụng khi bài đang ở trạng thái IN_PROGRESS.
     */
    CancelLessonResponse cancelLesson(Long lessonId, Long userId);
}