package com.example.nihongo_app.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubmitAlphabetPracticeRequest {

    public SubmitAlphabetPracticeRequest(List<Result> results) {
        this.results = results;
    }

    @NotEmpty
    @Valid
    private List<Result> results;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Result {
        private Long characterId;
        private Boolean isCorrect;
    }
}