package com.example.nihongo_app.dto.request;

import jakarta.validation.constraints.NotNull;

public class AnswerRequest {
    @NotNull(message = "questionId là bắt buộc")
    private Long questionId;

    @NotNull(message = "selectedOptionId là bắt buộc")
    private Long selectedOptionId;

    private Integer timeSpentSeconds;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public Integer getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Integer timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }
}
