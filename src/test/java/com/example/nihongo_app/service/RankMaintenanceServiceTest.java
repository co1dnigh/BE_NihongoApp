package com.example.nihongo_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.RankRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RankMaintenanceServiceTest {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock
    private UserRepository userRepository;

    @Mock
    private RankRepository rankRepository;

    @Mock
    private EmailService emailService;

    private RankMaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        maintenanceService = new RankMaintenanceService(userRepository, rankRepository, emailService);
        ReflectionTestUtils.setField(maintenanceService, "inactivityDays", 7);
        ReflectionTestUtils.setField(maintenanceService, "decayExp", 100);
        ReflectionTestUtils.setField(maintenanceService, "reminderEnabled", true);
    }

    @Test
    void applyWeeklyRankDecay_appliesAllMissedPeriodsAndRecalculatesRank() {
        Rank bronze = Rank.builder().id(1L).name("BRONZE").minExpRequired(0).orderIndex(1).build();
        User user = User.builder()
                .id(10L)
                .email("user@example.com")
                .exp(500)
                .rank(Rank.builder().id(2L).name("SILVER").minExpRequired(1000).orderIndex(2).build())
                .lastLearningAt(LocalDateTime.now(VIETNAM_ZONE).minusDays(15))
                .build();

        when(userRepository.findAllByDeletedAtIsNullAndLastLearningAtIsNotNull()).thenReturn(List.of(user));
        when(rankRepository.findFirstByMinExpRequiredLessThanEqualOrderByOrderIndexDesc(300))
                .thenReturn(Optional.of(bronze));

        maintenanceService.applyWeeklyRankDecay();

        assertThat(user.getExp()).isEqualTo(300);
        assertThat(user.getRank()).isSameAs(bronze);
        assertThat(user.getLastRankDecayAt()).isEqualTo(user.getLastLearningAt().plusDays(14));
        verify(userRepository).save(user);
    }

    @Test
    void sendRankReminders_sendsOnceWhenUserReachesReminderWindow() {
        User user = User.builder()
                .id(10L)
                .email("user@example.com")
                .displayName("User")
                .lastLearningAt(LocalDateTime.now(VIETNAM_ZONE).minusDays(6).minusHours(1))
                .build();

        when(userRepository.findAllByDeletedAtIsNullAndLastLearningAtIsNotNull()).thenReturn(List.of(user));

        maintenanceService.sendRankReminders();

        assertThat(user.getLastRankReminderAt()).isNotNull();
        verify(emailService).sendRankReminder(eq("user@example.com"), eq("User"), eq(1), eq(100));
        verify(userRepository).save(user);
    }
}