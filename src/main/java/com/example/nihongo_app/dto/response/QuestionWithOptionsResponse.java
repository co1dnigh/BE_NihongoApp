package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.JsonNode;

@Value
@Builder
public class QuestionWithOptionsResponse {
    Long id;
    Long lessonId;
    String questionType;
    String questionText;
    String audioUrl;
    String imageUrl;
    JsonNode metadataJson;
    List<OptionResponse> options;

    @Value
    @Builder
    public static class OptionResponse {
        Long id;
        String optionText;
        String imageUrl;
        String audioUrl;
        Boolean isCorrect;
        Integer orderIndex;
        JsonNode metadataJson;
    }
}
