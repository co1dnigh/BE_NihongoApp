package com.example.nihongo_app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AlphabetPracticeResponse {
    String practiceSessionId;
    List<Question> questions;

    @Value
    @Builder
    public static class Question {
        Long characterId;
        String questionType;
        String prompt;
        String symbol;
        String romaji;
        String audioUrl;
        String strokeOrderData;
        List<Option> options;
    }

    @Value
    @Builder
    public static class Option {
        Long optionId;
        String content;
        boolean correct;

        @JsonProperty("isCorrect")
        public boolean isCorrect() {
            return correct;
        }
    }
}