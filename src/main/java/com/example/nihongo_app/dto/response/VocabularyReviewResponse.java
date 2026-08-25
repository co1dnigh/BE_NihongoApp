package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Data;

/** Kết quả sau khi áp phiên ôn vào lịch. */
@Data
@Builder
public class VocabularyReviewResponse {
    private int reviewedCount;
    private int correctCount;
    /** Số từ CÒN LẠI đang tới hạn sau phiên này — để client cập nhật badge ngay. */
    private long remainingDue;
}
