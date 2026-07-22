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
public class ExercisePlayResponse {

    private Long id;
    private String exerciseType;
    private String questionText;
    private List<String> options;
}
