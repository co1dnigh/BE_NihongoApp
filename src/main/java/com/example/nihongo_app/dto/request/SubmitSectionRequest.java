package com.example.nihongo_app.dto.request;

import java.util.List;
import jakarta.validation.constraints.NotNull;

public class SubmitSectionRequest {
    @NotNull(message = "answers không được null")
    private List<AnswerRequest> answers;

    public List<AnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerRequest> answers) {
        this.answers = answers;
    }
}
