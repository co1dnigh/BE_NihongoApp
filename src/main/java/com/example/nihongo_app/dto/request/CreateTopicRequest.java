package com.example.nihongo_app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTopicRequest {

    @NotBlank
    private String title;

    private String description;

    @Min(0)
    private Integer orderIndex;
}
