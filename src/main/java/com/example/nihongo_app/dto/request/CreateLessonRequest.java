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

    @AssertTrue(message = "orderIndex is required for NORMAL and TOPIC_REVIEW lessons (must be null/omitted for JUMP_TEST and TIMED_REVIEW)")
    public boolean isOrderIndexValid() {
        if (lessonType == null) return true;
        // TOPIC_REVIEW nam tren duong di chinh (nhu NORMAL) nen can orderIndex.
        // TIMED_REVIEW ("on tap tinh gio") dung ben canh duong di, khong co vi tri
        // tuan tu tren path, nen khong yeu cau orderIndex -- giong JUMP_TEST.
        boolean pathLesson = lessonType == LessonType.NORMAL || lessonType == LessonType.TOPIC_REVIEW;
        if (pathLesson) {
            return orderIndex != null;
        }
        return true;
    }
}