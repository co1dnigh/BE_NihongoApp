package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StreakResponse {

    Integer currentStreak;
    Integer longestStreak;
    String lastStreakDate;
    Integer streakFreezeCount;
}
