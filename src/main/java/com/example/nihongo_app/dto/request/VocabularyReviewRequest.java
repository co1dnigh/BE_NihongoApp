package com.example.nihongo_app.dto.request;

import lombok.Data;

import java.util.List;

/** Kết quả một phiên ôn từ vựng do client gửi lên. */
@Data
public class VocabularyReviewRequest {

    private List<Item> results;

    @Data
    public static class Item {
        private Long vocabularyId;
        /** Người học nhớ được từ này hay không. */
        private Boolean correct;
    }
}
