package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.ChestOpenResponse;
import com.example.nihongo_app.dto.response.ChestStatusResponse;
import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.CoinTransaction.TransactionType;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserDailyQuest;
import com.example.nihongo_app.exception.ChestNotAvailableException;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.CoinTransactionRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rương thưởng hàng ngày (Daily Chest): mở được 1 lần/ngày, với điều kiện đã hoàn
 * thành đủ 3 Daily Quest trong ngày (xem {@link DailyQuestService}). Phần thưởng
 * là 1 lượng coin ngẫu nhiên, được roll ở server (tránh gian lận phía client).
 */
@Service
@RequiredArgsConstructor
public class ChestService {

    private static final int MIN_REWARD_COINS = 30;
    private static final int MAX_REWARD_COINS = 100;

    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final DailyQuestService dailyQuestService;

    @Transactional
    public ChestStatusResponse getStatus(Long userId) {
        User user = getUser(userId);
        List<UserDailyQuest> todayQuests = dailyQuestService.getTodayQuests(userId);
        long completedCount = todayQuests.stream().filter(q -> Boolean.TRUE.equals(q.getCompleted())).count();
        boolean alreadyOpenedToday = Objects.equals(user.getLastChestOpenedDate(), LocalDate.now());
        boolean allQuestsCompleted = !todayQuests.isEmpty() && completedCount == todayQuests.size();

        return ChestStatusResponse.builder()
                .available(allQuestsCompleted && !alreadyOpenedToday)
                .alreadyOpenedToday(alreadyOpenedToday)
                .questsCompleted((int) completedCount)
                .questsRequired(todayQuests.size())
                .build();
    }

    @Transactional
    public ChestOpenResponse openChest(Long userId) {
        User user = getUser(userId);
        LocalDate today = LocalDate.now();

        if (Objects.equals(user.getLastChestOpenedDate(), today)) {
            throw new ChestNotAvailableException("Ban da mo ruong hom nay roi, quay lai vao ngay mai.");
        }
        if (!dailyQuestService.areAllTodayQuestsCompleted(userId)) {
            throw new ChestNotAvailableException("Ban can hoan thanh du 3 nhiem vu hang ngay truoc khi mo ruong.");
        }

        int reward = ThreadLocalRandom.current().nextInt(MIN_REWARD_COINS, MAX_REWARD_COINS + 1);
        int currentCoins = Objects.requireNonNullElse(user.getCoins(), 0);
        int newCoins = currentCoins + reward;
        user.setCoins(newCoins);
        user.setLastChestOpenedDate(today);
        userRepository.save(user);

        coinTransactionRepository.save(CoinTransaction.builder()
                .userId(userId)
                .amount(reward)
                .transactionType(TransactionType.DAILY_CHEST)
                .build());

        return ChestOpenResponse.builder()
                .coinsRewarded(reward)
                .currentCoins(newCoins)
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user voi id=" + userId));
    }
}
