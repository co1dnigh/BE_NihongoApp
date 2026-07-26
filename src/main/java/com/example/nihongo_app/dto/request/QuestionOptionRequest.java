package com.example.nihongo_app.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
public class QuestionOptionRequest {

    private String optionText;
    private String imageUrl;
    private String audioUrl;

    @NotNull
    private Boolean isCorrect;

    private Integer orderIndex;
    private JsonNode metadataJson;
}