package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.response.LeaderboardResponse;
import com.example.nihongo_app.dto.response.RankResponse;
import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.RankRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RankServiceImplTest {

    @Mock
    private RankRepository rankRepository;

    @Mock
    private UserRepository userRepository;

    private RankServiceImpl rankService;

    private Rank rankBronze;
    private Rank rankSilver;
    private Rank rankGold;

    @BeforeEach
    void setUp() {
        rankService = new RankServiceImpl(rankRepository, userRepository);

        rankBronze = Rank.builder().id(1L).name("BRONZE").minExpRequired(0).orderIndex(1).build();
        rankSilver = Rank.builder().id(2L).name("SILVER").minExpRequired(1000).orderIndex(2).build();
        rankGold = Rank.builder().id(3L).name("GOLD").minExpRequired(3000).orderIndex(3).build();
    }

    @Test
    void getAllRanks_returnsOrderedRankList() {
        when(rankRepository.findAllByOrderByOrderIndexAsc())
                .thenReturn(List.of(rankBronze, rankSilver, rankGold));

        List<RankResponse> result = rankService.getAllRanks();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("BRONZE");
        assertThat(result.get(1).getName()).isEqualTo("SILVER");
        assertThat(result.get(2).getName()).isEqualTo("GOLD");
    }

    @Test
    void getLeaderboard_whenViewingOwnRank_returnsPositionAndNullMessage() {
        User currentUser = User.builder()
                .id(10L)
                .displayName("Current User")
                .username("curuser")
                .exp(1500)
                .rank(rankSilver)
                .build();

        User topUser1 = User.builder()
                .id(1L)
                .displayName("Top 1")
                .username("top1")
                .exp(2500)
                .rank(rankSilver)
                .build();

        when(rankRepository.findById(2L)).thenReturn(Optional.of(rankSilver));
        when(userRepository.findById(10L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findTop15ByRankIdOrderByExpDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(topUser1, currentUser));
        when(userRepository.countUsersWithExpGreaterThanInRank(2L, 1500)).thenReturn(3L);

        LeaderboardResponse response = rankService.getLeaderboard(2L, 10L);

        assertThat(response.getCurrentRankInfo().getRankId()).isEqualTo(2L);
        assertThat(response.getCurrentRankInfo().getName()).isEqualTo("SILVER");
        assertThat(response.getTopUsers()).hasSize(2);
        assertThat(response.getCurrentUserStanding().getUserId()).isEqualTo(10L);
        assertThat(response.getCurrentUserStanding().getPosition()).isEqualTo(4); // 3 + 1
        assertThat(response.getCurrentUserStanding().getMessage()).isNull();
    }

    @Test
    void getLeaderboard_whenViewingDifferentRank_returnsNullPositionAndEncouragingMessage() {
        User currentUser = User.builder()
                .id(10L)
                .displayName("Current User")
                .username("curuser")
                .exp(1200)
                .rank(rankSilver) // User is in Silver
                .build();

        User goldUser = User.builder()
                .id(20L)
                .displayName("Gold Master")
                .username("goldmaster")
                .exp(3500)
                .rank(rankGold)
                .build();

        when(rankRepository.findById(3L)).thenReturn(Optional.of(rankGold));
        when(userRepository.findById(10L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findTop15ByRankIdOrderByExpDesc(eq(3L), any(Pageable.class)))
                .thenReturn(List.of(goldUser));

        LeaderboardResponse response = rankService.getLeaderboard(3L, 10L);

        assertThat(response.getCurrentRankInfo().getRankId()).isEqualTo(3L);
        assertThat(response.getCurrentRankInfo().getName()).isEqualTo("GOLD");
        assertThat(response.getCurrentUserStanding().getPosition()).isNull();
        // Required EXP for Gold = 3000 - 1200 = 1800
        assertThat(response.getCurrentUserStanding().getMessage())
                .isEqualTo("Bạn cần thêm 1800 điểm nữa để gia nhập hạng này");
    }

    @Test
    void getLeaderboard_rankNotFound_throws404() {
        when(rankRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rankService.getLeaderboard(99L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Rank not found");
    }
}
