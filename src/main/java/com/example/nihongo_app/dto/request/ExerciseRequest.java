package com.example.nihongo_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRequest {

    @NotNull
    private Long lessonId;

    @NotBlank
    private String exerciseType;

    @NotBlank
    private String questionText;

    @NotBlank
    private String correctAnswer;

    @NotEmpty
    private List<String> options;
}
