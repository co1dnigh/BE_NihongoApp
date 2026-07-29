package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.JsonNode;

@Value
@Builder
public class LessonWithQuestionsResponse {
    Long id;
    Long topicId;
    String title;
    String lessonType;
    Integer orderIndex;
    JsonNode configJson;
    List<QuestionSummaryResponse> questions;

    @Value
    @Builder
    public static class QuestionSummaryResponse {
        Long id;
        String questionType;
        String questionText;
        String audioUrl;
        String imageUrl;
        Integer optionCount;
        Integer correctCount;
    }
}
