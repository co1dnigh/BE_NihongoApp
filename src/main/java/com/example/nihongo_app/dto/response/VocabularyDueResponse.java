package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Tóm tắt hàng đợi ôn tập: con số cho badge + danh sách từ để dựng phiên ôn. */
@Data
@Builder
public class VocabularyDueResponse {
    /** Số từ đã tới hạn — dùng cho badge "N từ cần ôn" trên tab Luyện tập. */
    private long dueCount;
    /** Tổng số từ người học đã gặp ít nhất một lần. */
    private long learnedCount;
    private List<VocabularyItemResponse> items;
}
