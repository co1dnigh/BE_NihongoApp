package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Response cho {@code GET /api/v1/reviews/mistakes/summary}.
 */
@Value
@Builder
public class MistakeSummaryResponse {

    /** Tổng số câu hỏi đang ở trạng thái ACTIVE trong Mistake Bank của user. */
    long activeCount;

    /**
     * Số phiên ôn còn được thưởng năng lượng trong hôm nay (0..rewarded-sessions-per-day).
     * FE dùng để hiển thị vd "Ôn ngay +5⚡ (còn 2 lượt)".
     */
    int reviewableToday;
}
