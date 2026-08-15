package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LeaderboardResponse {

    RankResponse currentRankInfo;
    List<LeaderboardUserResponse> topUsers;
    CurrentUserStandingResponse currentUserStanding;

    @Value
    @Builder
    public static class LeaderboardUserResponse {
        Long userId;
        String displayName;
        String username;
        Integer level;
        Integer exp;
        Long rankId;
        String rankName;
        Integer position;
    }

    @Value
    @Builder
    public static class CurrentUserStandingResponse {
        Long userId;
        Integer exp;
        Integer position;
        String message;
    }
}
