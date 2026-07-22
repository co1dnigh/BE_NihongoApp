package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAdminResponse {

    private Long id;
    private Long lessonId;
    private String exerciseType;
    private String questionText;
    private String correctAnswer;
    private List<String> options;
}
