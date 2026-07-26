package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.Lesson.LessonType;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.JsonNode;

@Value
@Builder
public class LessonResponse {
    Long id;
    Long topicId;
    String title;
    LessonType lessonType;
    Integer orderIndex;
    JsonNode configJson;
}
