package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.ReviewSubmitRequest;
import com.example.nihongo_app.dto.response.MistakeSummaryResponse;
import com.example.nihongo_app.dto.response.ReviewSessionResponse;
import com.example.nihongo_app.dto.response.ReviewSubmitResponse;
import com.example.nihongo_app.entity.LessonAttemptAnswer;
import java.util.List;

/**
 * Mistake Bank: theo dõi các câu hỏi user hay sai, cho phép ôn lại riêng qua 1 phiên ôn
 * (start/submit) tách biệt với luồng học bài thường.
 */
public interface MistakeService {

    /**
     * Ghi nhận các câu SAI trong 1 lượt làm bài học thường vào Mistake Bank (upsert theo
     * user+question). Câu ĐÚNG không tác động gì tới Mistake Bank — chỉ phiên ôn lỗi sai
     * mới có thể "xoá nợ" (chuyển RESOLVED).
     *
     * @param gradedAnswers danh sách answer đã được {@code LessonAttemptServiceImpl} chấm
     *                       (is_correct xác định từ DB) ở bước ghi lesson_attempt_answers.
     */
    void recordFromAnswers(Long userId, List<LessonAttemptAnswer> gradedAnswers);

    /** {@code GET /summary}: số mistake ACTIVE + số phiên còn được thưởng năng lượng hôm nay. */
    MistakeSummaryResponse getSummary(Long userId);

    /**
     * {@code POST /start}: tạo phiên ôn tập từ tối đa {@code session-size} mistake ACTIVE
     * (ưu tiên sai nhiều nhất, sai gần nhất), câu hỏi/đáp án đã shuffle, không lộ đáp án đúng,
     * không trừ năng lượng.
     */
    ReviewSessionResponse startReviewSession(Long userId);

    /**
     * {@code POST /submit}: chấm từng câu bằng DB, cập nhật wrong_count/correct_streak/status
     * tương ứng, thưởng năng lượng nếu hợp lệ và còn lượt trong ngày.
     */
    ReviewSubmitResponse submitReviewSession(Long userId, ReviewSubmitRequest request);
}
