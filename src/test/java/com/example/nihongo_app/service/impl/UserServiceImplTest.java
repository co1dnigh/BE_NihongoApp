package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.response.UserStatsResponse;
import com.example.nihongo_app.entity.League;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.EnergyService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

/**
 * Chi test {@link UserServiceImpl#getMyStats}, API tong hop trang thai gamification moi
 * them (GET /api/v1/users/me) de FE hydrate UI ngay sau khi co token.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EnergyService energyService;

    private UserServiceImpl userService;

    private static final String EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, jwtTokenProvider, energyService);
    }

    @Test
    void getMyStats_returnsFullGamificationSnapshot() {
        User user = User.builder()
                .id(1L).email(EMAIL).displayName("Test User").username("testuser").role("LEARNER")
                .level(3).exp(120).currentLeague(League.SILVER)
                .coins(85)
                .currentEnergy(15).maxEnergy(25)
                .currentStreak(5).longestStreak(10).streakFreezeCount(1)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserStatsResponse stats = userService.getMyStats(EMAIL);

        assertThat(stats.getId()).isEqualTo(1L);
        assertThat(stats.getEmail()).isEqualTo(EMAIL);
        assertThat(stats.getLevel()).isEqualTo(3);
        assertThat(stats.getExp()).isEqualTo(120);
        assertThat(stats.getCurrentLeague()).isEqualTo(League.SILVER);
        assertThat(stats.getCoins()).isEqualTo(85);
        assertThat(stats.getCurrentEnergy()).isEqualTo(15);
        assertThat(stats.getMaxEnergy()).isEqualTo(25);
        assertThat(stats.getCurrentStreak()).isEqualTo(5);
        assertThat(stats.getLongestStreak()).isEqualTo(10);
        assertThat(stats.getStreakFreezeCount()).isEqualTo(1);

        // Phai hoi nang luong thu dong truoc khi doc, de currentEnergy luon chinh xac.
        verify(energyService).recoverEnergy(1L);
    }

    @Test
    void getMyStats_userNotFound_throws404() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyStats(EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }
}
