package com.example.nihongo_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.response.ChestOpenResponse;
import com.example.nihongo_app.dto.response.ChestStatusResponse;
import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.CoinTransaction.TransactionType;
import com.example.nihongo_app.entity.QuestDefinition.QuestType;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserDailyQuest;
import com.example.nihongo_app.exception.ChestNotAvailableException;
import com.example.nihongo_app.repository.CoinTransactionRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoinTransactionRepository coinTransactionRepository;

    @Mock
    private DailyQuestService dailyQuestService;

    private ChestService chestService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        chestService = new ChestService(userRepository, coinTransactionRepository, dailyQuestService);
    }

    private UserDailyQuest questWith(boolean completed) {
        return UserDailyQuest.builder().questType(QuestType.COMPLETE_LESSONS).targetValue(1)
                .currentProgress(completed ? 1 : 0).completed(completed).build();
    }

    @Test
    void getStatus_availableOnlyWhenAllQuestsDoneAndNotOpenedToday() {
        User user = User.builder().id(USER_ID).coins(0).lastChestOpenedDate(null).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(dailyQuestService.getTodayQuests(USER_ID))
                .thenReturn(List.of(questWith(true), questWith(true), questWith(true)));

        ChestStatusResponse response = chestService.getStatus(USER_ID);

        assertThat(response.getAvailable()).isTrue();
        assertThat(response.getAlreadyOpenedToday()).isFalse();
        assertThat(response.getQuestsCompleted()).isEqualTo(3);
        assertThat(response.getQuestsRequired()).isEqualTo(3);
    }

    @Test
    void getStatus_notAvailable_whenAlreadyOpenedToday() {
        User user = User.builder().id(USER_ID).coins(0).lastChestOpenedDate(LocalDate.now()).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(dailyQuestService.getTodayQuests(USER_ID))
                .thenReturn(List.of(questWith(true), questWith(true), questWith(true)));

        ChestStatusResponse response = chestService.getStatus(USER_ID);

        assertThat(response.getAvailable()).isFalse();
        assertThat(response.getAlreadyOpenedToday()).isTrue();
    }

    @Test
    void openChest_throws_whenAlreadyOpenedToday() {
        User user = User.builder().id(USER_ID).coins(0).lastChestOpenedDate(LocalDate.now()).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> chestService.openChest(USER_ID))
                .isInstanceOf(ChestNotAvailableException.class);
    }

    @Test
    void openChest_throws_whenQuestsIncomplete() {
        User user = User.builder().id(USER_ID).coins(0).lastChestOpenedDate(null).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(dailyQuestService.areAllTodayQuestsCompleted(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> chestService.openChest(USER_ID))
                .isInstanceOf(ChestNotAvailableException.class);
    }

    @Test
    void openChest_succeeds_rewardsWithinRangeAndLogsTransaction() {
        User user = User.builder().id(USER_ID).coins(50).lastChestOpenedDate(null).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(dailyQuestService.areAllTodayQuestsCompleted(USER_ID)).thenReturn(true);

        ChestOpenResponse response = chestService.openChest(USER_ID);

        assertThat(response.getCoinsRewarded()).isBetween(30, 100);
        assertThat(response.getCurrentCoins()).isEqualTo(50 + response.getCoinsRewarded());
        assertThat(user.getLastChestOpenedDate()).isEqualTo(LocalDate.now());

        ArgumentCaptor<CoinTransaction> captor = ArgumentCaptor.forClass(CoinTransaction.class);
        verify(coinTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.DAILY_CHEST);
        assertThat(captor.getValue().getAmount()).isEqualTo(response.getCoinsRewarded());
    }
}
