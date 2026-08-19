package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubmitAlphabetPracticeResponse {
    Integer expEarned;
    Integer currentExp;
    boolean promoted;
    String newRankName;
    String message;
}