package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.LeaderboardResponse;
import com.example.nihongo_app.dto.response.RankResponse;
import java.util.List;

public interface RankService {

    List<RankResponse> getAllRanks();

    LeaderboardResponse getLeaderboard(Long rankId, Long currentUserId);
}
