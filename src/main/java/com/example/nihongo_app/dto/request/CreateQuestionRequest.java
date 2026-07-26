package com.example.nihongo_app.dto.request;

import com.example.nihongo_app.entity.LessonQuestion.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
public class CreateQuestionRequest {

    @NotNull
    private Long lessonId;

    @NotNull
    private QuestionType questionType;

    private String questionText;
    private String audioUrl;
    private String imageUrl;
    private JsonNode metadataJson;

    @Valid
    @NotEmpty
    private List<QuestionOptionRequest> options;
}