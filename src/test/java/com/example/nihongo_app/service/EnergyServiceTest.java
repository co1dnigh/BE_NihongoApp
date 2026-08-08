package com.example.nihongo_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.exception.InsufficientCoinsException;
import com.example.nihongo_app.exception.InsufficientEnergyException;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test cho thiet ke nang luong moi: max 25, hoi tu nhien +5/gio, xem QC +5,
 * hoi day bang 400 coin.
 */
@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    @Mock
    private UserRepository userRepository;

    private EnergyService energyService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        energyService = new EnergyService(userRepository);
    }

    @Test
    void recoverEnergy_addsFivePerHourElapsed_cappedAtMax() {
        User user = User.builder().id(USER_ID).currentEnergy(0).maxEnergy(25)
                .lastEnergyResetDate(LocalDateTime.now().minusHours(2)).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        energyService.recoverEnergy(USER_ID);

        assertThat(user.getCurrentEnergy()).isEqualTo(10); // 2 gio * 5
    }

    @Test
    void recoverEnergy_cappedAtMaxEnergy_evenAfterManyHours() {
        User user = User.builder().id(USER_ID).currentEnergy(20).maxEnergy(25)
                .lastEnergyResetDate(LocalDateTime.now().minusHours(10)).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        energyService.recoverEnergy(USER_ID);

        assertThat(user.getCurrentEnergy()).isEqualTo(25);
    }

    @Test
    void watchAd_addsFiveEnergy_cappedAtMax() {
        User user = User.builder().id(USER_ID).currentEnergy(22).maxEnergy(25)
                .lastEnergyResetDate(LocalDateTime.now()).lastAdWatchDate(null).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        energyService.watchAd(USER_ID);

        assertThat(user.getCurrentEnergy()).isEqualTo(25); // 22+5=27 -> cap 25
    }

    @Test
    void watchAd_throwsWhenAlreadyFull() {
        User user = User.builder().id(USER_ID).currentEnergy(25).maxEnergy(25)
                .lastEnergyResetDate(LocalDateTime.now()).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> energyService.watchAd(USER_ID))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void watchAd_throwsWhenCooldownNotElapsed() {
        User user = User.builder().id(USER_ID).currentEnergy(0).maxEnergy(25)
                .lastEnergyResetDate(LocalDateTime.now())
                .lastAdWatchDate(LocalDateTime.now().minusMinutes(5)).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> energyService.watchAd(USER_ID))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deductEnergy_throwsWhenInsufficient() {
        User user = User.builder().id(USER_ID).currentEnergy(3).maxEnergy(25)
                .lastEnergyResetDate(LocalDateTime.now()).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> energyService.deductEnergy(USER_ID, 10))
                .isInstanceOf(InsufficientEnergyException.class);
    }

    @Test
    void deductEnergy_succeeds_whenEnough() {
        User user = User.builder().id(USER_ID).currentEnergy(15).maxEnergy(25)
                .lastEnergyResetDate(LocalDateTime.now()).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        int remaining = energyService.deductEnergy(USER_ID, 10);

        assertThat(remaining).isEqualTo(5);
        assertThat(user.getCurrentEnergy()).isEqualTo(5);
    }

    @Test
    void refillWithCoins_throwsWhenInsufficientCoins() {
        User user = User.builder().id(USER_ID).currentEnergy(0).maxEnergy(25).coins(100)
                .lastEnergyResetDate(LocalDateTime.now()).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> energyService.refillWithCoins(USER_ID))
                .isInstanceOf(InsufficientCoinsException.class);
    }

    @Test
    void refillWithCoins_succeeds_fillsToMaxAndDeductsCoins() {
        User user = User.builder().id(USER_ID).currentEnergy(5).maxEnergy(25).coins(500)
                .lastEnergyResetDate(LocalDateTime.now()).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        energyService.refillWithCoins(USER_ID);

        assertThat(user.getCurrentEnergy()).isEqualTo(25);
        assertThat(user.getCoins()).isEqualTo(100); // 500 - 400
    }
}
