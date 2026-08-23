package com.example.nihongo_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.CoinTransaction.TransactionType;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.exception.InsufficientCoinsException;
import com.example.nihongo_app.repository.AchievementRepository;
import com.example.nihongo_app.repository.CoinTransactionRepository;
import com.example.nihongo_app.repository.UserInventoryRepository;
import com.example.nihongo_app.repository.UserStreakDayRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.AchievementService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Chỉ test {@link StreakService#buyStreakFreeze}, phần mới thêm cho tính năng
 * coin/rương thưởng. {@code checkAndUpdateStreak} là code có sẵn của nhánh
 * EnegyStreak, không nằm trong phạm vi lần thay đổi này.
 */
@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInventoryRepository userInventoryRepository;

    @Mock
    private CoinTransactionRepository coinTransactionRepository;

    @Mock
    private ShopService shopService;

    @Mock
    private UserStreakDayRepository userStreakDayRepository;

    @Mock
    private AchievementService achievementService;

    private StreakService streakService;

    private static final Long USER_ID = 1L;
    private static final int FREEZE_COST_COINS = 200;

    @BeforeEach
    void setUp() {
        streakService = new StreakService(userRepository, userInventoryRepository,
                coinTransactionRepository, shopService, userStreakDayRepository, achievementService);
    }

    @Test
    void buyStreakFreeze_insufficientCoins_throwsAndDoesNotMutateUser() {
        User user = User.builder().id(USER_ID).coins(100).streakFreezeCount(0).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> streakService.buyStreakFreeze(USER_ID))
                .isInstanceOf(InsufficientCoinsException.class);

        assertThat(user.getCoins()).isEqualTo(100);
        assertThat(user.getStreakFreezeCount()).isZero();
        verifyNoInteractions(coinTransactionRepository);
    }

    @Test
    void buyStreakFreeze_sufficientCoins_deductsAndIncrementsFreezeCount() {
        User user = User.builder().id(USER_ID).coins(300).streakFreezeCount(1).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        User result = streakService.buyStreakFreeze(USER_ID);

        assertThat(result.getCoins()).isEqualTo(300 - FREEZE_COST_COINS);
        assertThat(result.getStreakFreezeCount()).isEqualTo(2);

        ArgumentCaptor<CoinTransaction> captor = ArgumentCaptor.forClass(CoinTransaction.class);
        verify(coinTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(-FREEZE_COST_COINS);
        assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.BUY_STREAK_FREEZE);
    }
}
