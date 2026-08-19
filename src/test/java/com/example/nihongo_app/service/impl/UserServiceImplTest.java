package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.response.UserStatsResponse;
import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.UserActiveEffect;
import com.example.nihongo_app.repository.UserActiveEffectRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.EnergyService;
import java.time.LocalDateTime;
import java.util.List;
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

    @Mock
    private UserActiveEffectRepository userActiveEffectRepository;

    private UserServiceImpl userService;

    private static final String EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, jwtTokenProvider, energyService, userActiveEffectRepository);
    }

    @Test
    void getMyStats_returnsFullGamificationSnapshot() {
        Rank rank = Rank.builder().id(2L).name("SILVER").minExpRequired(1000).orderIndex(2).build();

        User user = User.builder()
                .id(1L).email(EMAIL).displayName("Test User").username("testuser").role("LEARNER")
                .level(3).exp(120).rank(rank)
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
        assertThat(stats.getRankId()).isEqualTo(2L);
        assertThat(stats.getRankName()).isEqualTo("SILVER");
        assertThat(stats.getCoins()).isEqualTo(85);
        assertThat(stats.getCurrentEnergy()).isEqualTo(15);
        assertThat(stats.getMaxEnergy()).isEqualTo(25);
        assertThat(stats.getCurrentStreak()).isEqualTo(5);
        assertThat(stats.getLongestStreak()).isEqualTo(10);
        assertThat(stats.getStreakFreezeCount()).isEqualTo(1);
        assertThat(stats.getActiveEffects()).isEmpty();

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

    @Test
    void getMyStats_withActiveEffects_mapsCorrectly() {
        Rank rank = Rank.builder().id(2L).name("SILVER").minExpRequired(1000).orderIndex(2).build();
        User user = User.builder()
                .id(1L).email(EMAIL).displayName("Test User").username("testuser").role("LEARNER")
                .level(3).exp(120).rank(rank)
                .coins(85).currentEnergy(15).maxEnergy(25)
                .currentStreak(5).longestStreak(10).streakFreezeCount(1)
                .build();
        
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        UserActiveEffect effect = UserActiveEffect.builder()
                .userId(1L)
                .effectType(ShopItem.EffectType.DOUBLE_XP)
                .expiresAt(expiresAt)
                .build();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userActiveEffectRepository.findByUserIdAndExpiresAtAfter(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(effect));

        UserStatsResponse stats = userService.getMyStats(EMAIL);

        assertThat(stats.getActiveEffects()).hasSize(1);
        assertThat(stats.getActiveEffects().get(0).getEffectType()).isEqualTo("DOUBLE_XP");
        assertThat(stats.getActiveEffects().get(0).getExpiresAt()).isEqualTo(expiresAt);
    }
}
