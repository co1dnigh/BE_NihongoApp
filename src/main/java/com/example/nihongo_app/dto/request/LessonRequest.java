package com.example.nihongo_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank
    @Size(max = 10)
    private String jlptLevel;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    private Integer orderIndex;
}
