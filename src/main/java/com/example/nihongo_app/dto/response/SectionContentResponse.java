package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionContentResponse {
    private Long sectionId;
    private String title;
    private Integer timeLimitMinutes;
    private Integer timeRemainingSeconds;
    private List<QuestionResponse> questions;

    @Data
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String type;
        private String audioUrl;
        private String imageUrl;
        private List<OptionResponse> options;
    }

    @Data
    @Builder
    public static class OptionResponse {
        private Long id;
        private String optionText;
    }
}
