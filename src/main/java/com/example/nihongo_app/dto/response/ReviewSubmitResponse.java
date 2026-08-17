package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Response cho {@code POST /api/v1/reviews/mistakes/submit}.
 */
@Value
@Builder
public class ReviewSubmitResponse {

    /** Kết quả chấm từng câu (server tự đối chiếu DB, không tin FE). */
    List<ReviewAnswerResult> results;

    /** Tổng số câu vừa được chuyển RESOLVED ("xoá nợ") trong lần nộp này. */
    int resolvedCount;

    /** Năng lượng vừa được thưởng (0 nếu đã hết lượt thưởng trong ngày). */
    int energyRewarded;

    /** Năng lượng hiện tại của user sau khi cộng thưởng, để FE cập nhật UI không cần gọi thêm API. */
    Integer currentEnergy;

    @Value
    @Builder
    public static class ReviewAnswerResult {
        Long questionId;
        Boolean correct;
        /** Option đúng của câu hỏi này, để FE hiển thị đáp án đúng sau khi chấm. */
        Long correctOptionId;
        /** {@code true} nếu câu này vừa khiến mistake tương ứng chuyển sang RESOLVED. */
        Boolean resolved;
    }
}
