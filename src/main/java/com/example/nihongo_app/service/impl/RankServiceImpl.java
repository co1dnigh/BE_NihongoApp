package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.response.LeaderboardResponse;
import com.example.nihongo_app.dto.response.LeaderboardResponse.CurrentUserStandingResponse;
import com.example.nihongo_app.dto.response.LeaderboardResponse.LeaderboardUserResponse;
import com.example.nihongo_app.dto.response.RankResponse;
import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.RankRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.RankService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RankServiceImpl implements RankService {

    private final RankRepository rankRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RankResponse> getAllRanks() {
        return rankRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(this::toRankResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "leaderboard", key = "#rankId + ':' + #currentUserId", cacheManager = "cacheManager")
    public LeaderboardResponse getLeaderboard(Long rankId, Long currentUserId) {
        Rank rank = rankRepository.findById(rankId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rank not found"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(0, 15);
        List<User> topUsers = userRepository.findTop15ByRankIdOrderByExpDesc(rankId, pageable);

        List<LeaderboardUserResponse> topUserResponses = topUsers.stream()
                .map(user -> LeaderboardUserResponse.builder()
                        .userId(user.getId())
                        .displayName(user.getDisplayName())
                        .username(user.getUsername())
                        .level(user.getLevel())
                        .exp(user.getExp())
                        .rankId(user.getRank() != null ? user.getRank().getId() : null)
                        .rankName(user.getRank() != null ? user.getRank().getName() : null)
                        .position(null)
                        .build())
                .toList();

        Integer currentPosition = null;
        String standingMessage = null;
        if (Objects.equals(currentUser.getRank() != null ? currentUser.getRank().getId() : null, rankId)) {
            currentPosition = resolveUserPosition(rankId, currentUser.getExp());
        } else {
            int requiredExp = resolveRequiredExpToJoinRank(rankId, currentUser.getExp());
            if (requiredExp > 0) {
                standingMessage = "Bạn cần thêm " + requiredExp + " điểm nữa để gia nhập hạng này";
            }
        }

        return LeaderboardResponse.builder()
                .currentRankInfo(toRankResponse(rank))
                .topUsers(topUserResponses)
                .currentUserStanding(CurrentUserStandingResponse.builder()
                        .userId(currentUser.getId())
                        .exp(currentUser.getExp())
                        .position(currentPosition)
                        .message(standingMessage)
                        .build())
                .build();
    }

    private Integer resolveUserPosition(Long rankId, Integer currentExp) {
        Long count = userRepository.countUsersWithExpGreaterThanInRank(rankId, currentExp);
        return Math.toIntExact(count + 1L);
    }

    private int resolveRequiredExpToJoinRank(Long rankId, Integer currentExp) {
        Rank targetRank = rankRepository.findById(rankId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rank not found"));

        Integer userExp = Optional.ofNullable(currentExp).orElse(0);
        int required = targetRank.getMinExpRequired() - userExp;
        return Math.max(0, required);
    }

    private RankResponse toRankResponse(Rank rank) {
        return RankResponse.builder()
                .rankId(rank.getId())
                .name(rank.getName())
                .minExpRequired(rank.getMinExpRequired())
                .orderIndex(rank.getOrderIndex())
                .build();
    }
}
