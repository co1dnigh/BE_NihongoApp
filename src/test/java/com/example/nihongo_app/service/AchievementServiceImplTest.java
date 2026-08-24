package com.example.nihongo_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.entity.Achievement;
import com.example.nihongo_app.entity.UserAchievement;
import com.example.nihongo_app.repository.AchievementRepository;
import com.example.nihongo_app.repository.UserAchievementRepository;
import com.example.nihongo_app.service.impl.AchievementServiceImpl;
import com.example.nihongo_app.service.PostService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test AchievementService: unlock logic, secret achievements, progress tracking.
 */
@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private PostService postService;

    private AchievementServiceImpl achievementService;

    @BeforeEach
    void setUp() {
        achievementService = new AchievementServiceImpl(achievementRepository, userAchievementRepository, postService);
    }

    @Test
    void onEvent_thresholdReached_unlocksAchievement() {
        Achievement a = Achievement.builder().id(10L).code("STREAK_7").type("STREAK_MILESTONE")
                .threshold(7).secret(false).active(true).build();
        when(achievementRepository.findAllByActiveTrue()).thenReturn(List.of(a));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, 10L))
                .thenReturn(Optional.empty());

        achievementService.onEvent(USER_ID, AchievementProgress.of("STREAK_MILESTONE", 7));

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void onEvent_thresholdNotReached_onlyProgressUpdatedNoUnlock() {
        Achievement a = Achievement.builder().id(10L).code("STREAK_7").type("STREAK_MILESTONE")
                .threshold(7).secret(false).active(true).build();
        when(achievementRepository.findAllByActiveTrue()).thenReturn(List.of(a));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, 10L))
                .thenReturn(Optional.empty());

        achievementService.onEvent(USER_ID, AchievementProgress.of("STREAK_MILESTONE", 3));

        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
    }

    @Test
    void onEvent_alreadyUnlocked_doesNotUnlockAgain() {
        Achievement a = Achievement.builder().id(10L).code("STREAK_7").type("STREAK_MILESTONE")
                .threshold(7).secret(false).active(true).build();
        UserAchievement ua = UserAchievement.builder()
                .id(1L).userId(USER_ID).achievement(a).unlockedAt(LocalDateTime.now()).progress(7).build();
        when(achievementRepository.findAllByActiveTrue()).thenReturn(List.of(a));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, 10L))
                .thenReturn(Optional.of(ua));

        achievementService.onEvent(USER_ID, AchievementProgress.of("STREAK_MILESTONE", 10));

        // unlockedAt already set → no new save.
        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
        assertThat(ua.getProgress()).isEqualTo(10);
    }

    @Test
    void onEvent_progressTakesMaxValue() {
        Achievement a = Achievement.builder().id(20L).code("COIN_MASTER").type("COIN_EARNED")
                .threshold(1000).secret(false).active(true).build();
        UserAchievement ua = UserAchievement.builder()
                .id(1L).userId(USER_ID).achievement(a).unlockedAt(null).progress(500).build();
        when(achievementRepository.findAllByActiveTrue()).thenReturn(List.of(a));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, 20L))
                .thenReturn(Optional.of(ua));

        achievementService.onEvent(USER_ID, AchievementProgress.of("COIN_EARNED", 700));

        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
        assertThat(ua.getProgress()).isEqualTo(700);
    }

    @Test
    void onEvent_earlyBird_unlocksWhenHourBefore9() {
        Achievement a = Achievement.builder().id(30L).code("EARLY_BIRD").type("DAILY_STUDY")
                .threshold(1).secret(true).active(true).build();
        when(achievementRepository.findAllByActiveTrue()).thenReturn(List.of(a));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, 30L))
                .thenReturn(Optional.empty());

        achievementService.onEvent(USER_ID, AchievementProgress.of("DAILY_STUDY", 1, "7"));

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void onEvent_nightOwl_doesNotUnlockAtHour20() {
        Achievement a = Achievement.builder().id(31L).code("NIGHT_OWL").type("DAILY_STUDY")
                .threshold(1).secret(true).active(true).build();
        lenient().when(achievementRepository.findAllByActiveTrue()).thenReturn(List.of(a));
        lenient().when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, 31L))
                .thenReturn(Optional.empty());

        achievementService.onEvent(USER_ID, AchievementProgress.of("DAILY_STUDY", 1, "20"));

        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
    }

    @Test
    void onEvent_comeback_unlocksWhenPreviousStreakAtLeast7() {
        Achievement a = Achievement.builder().id(40L).code("COMEBACK").type("STREAK_RESET")
                .threshold(7).secret(true).active(true).build();
        when(achievementRepository.findByCodeAndActiveTrue("COMEBACK")).thenReturn(Optional.of(a));
        when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, 40L))
                .thenReturn(Optional.empty());

        achievementService.onEvent(USER_ID, AchievementProgress.of("STREAK_RESET", 7, "14"));

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void onEvent_comeback_doesNotUnlockWhenPreviousStreakBelowThreshold() {
        Achievement a = Achievement.builder().id(40L).code("COMEBACK").type("STREAK_RESET")
                .threshold(7).secret(true).active(true).build();
        lenient().when(achievementRepository.findByCodeAndActiveTrue("COMEBACK")).thenReturn(Optional.of(a));
        lenient().when(userAchievementRepository.findByUserIdAndAchievementId(USER_ID, 40L))
                .thenReturn(Optional.empty());

        achievementService.onEvent(USER_ID, AchievementProgress.of("STREAK_RESET", 7, "3"));

        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
    }

    @Test
    void onEvent_nullOrBlankProgress_isNoop() {
        achievementService.onEvent(USER_ID, null);
        achievementService.onEvent(USER_ID, AchievementProgress.of("", 1));
        verify(achievementRepository, never()).findAllByActiveTrue();
    }

    @Test
    void listAchievements_hidesSecretUntilUnlocked() {
        Achievement secret = Achievement.builder().id(50L).code("UNSTOPPABLE").type("STREAK_MILESTONE")
                .threshold(365).secret(true).active(true).build();
        Achievement normal = Achievement.builder().id(51L).code("STREAK_7").type("STREAK_MILESTONE")
                .threshold(7).secret(false).active(true).build();
        when(achievementRepository.findAllByActiveTrue()).thenReturn(List.of(secret, normal));
        when(userAchievementRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        List<Achievement> result = achievementService.listAchievements(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("STREAK_7");
    }
}