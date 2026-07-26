package com.example.nihongo_app.dto.request;

import com.example.nihongo_app.entity.Lesson.LessonType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
public class CreateLessonRequest {

    @NotNull
    private Long topicId;

    @NotNull
    private String title;

    @NotNull
    private LessonType lessonType;

    @Min(0)
    private Integer orderIndex;

    private JsonNode configJson;

    @AssertTrue(message = "orderIndex is required for NORMAL and TIMED_REVIEW lessons (must be null/omitted for JUMP_TEST)")
    public boolean isOrderIndexValid() {
        if (lessonType == null) return true;
        boolean visualLesson = lessonType == LessonType.NORMAL || lessonType == LessonType.TIMED_REVIEW;
        if (visualLesson) {
            return orderIndex != null;
        }
        return true;
    }
}