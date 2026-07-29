package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TopicWithLessonsResponse {
    Long id;
    String title;
    String description;
    Integer orderIndex;
    List<LessonSummaryResponse> lessons;

    @Value
    @Builder
    public static class LessonSummaryResponse {
        Long id;
        String title;
        String lessonType;
        Integer orderIndex;
        Integer questionCount;
    }
}
