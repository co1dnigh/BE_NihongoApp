package com.example.nihongo_app.service;

import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.CoinTransaction.TransactionType;
import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserInventory;
import com.example.nihongo_app.exception.InsufficientCoinsException;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.CoinTransactionRepository;
import com.example.nihongo_app.repository.UserInventoryRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserRepository userRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final ShopService shopService;
    private static final int FREEZE_AWARD_THRESHOLD = 10;
    private static final int FREEZE_COST_COINS = 200;

    @Transactional
    public void checkAndUpdateStreak(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getLastStreakDate();
        int currentStreak = Objects.requireNonNullElse(user.getCurrentStreak(), 0);
        int freezeCount = Objects.requireNonNullElse(user.getStreakFreezeCount(), 0);
        boolean awarded = Boolean.TRUE.equals(user.getStreakFreezeAwarded());
        int newStreak;
        boolean updated = false;

        if (lastDate == null) {
            newStreak = 1;
            updated = true;
        } else if (lastDate.equals(today)) {
            newStreak = currentStreak;
        } else if (lastDate.equals(today.minusDays(1))) {
            newStreak = currentStreak + 1;
            updated = true;
        } else {
            // Check for streak freeze: first use inventory item, then use streakFreezeCount
            boolean usedFreeze = false;

            // 1. Try to consume streak freeze from inventory
            if (shopService.hasActivePowerup(userId, ShopItem.EffectType.STREAK_FREEZE)) {
                // Find and consume the streak freeze item from inventory
                var inventoryItems = userInventoryRepository.findAllByUserId(userId);
                for (UserInventory inv : inventoryItems) {
                    if (inv.getQuantity() > 0) {
                        var shopItem = shopService.getShopItems().values().stream()
                            .flatMap(List::stream)
                            .filter(si -> si.getId().equals(inv.getItemId()))
                            .findFirst()
                            .orElse(null);
                        if (shopItem != null && shopItem.getEffectType() == ShopItem.EffectType.STREAK_FREEZE) {
                            // Consume one from inventory
                            inv.setQuantity(inv.getQuantity() - 1);
                            if (inv.getQuantity() == 0) {
                                userInventoryRepository.delete(inv);
                            } else {
                                userInventoryRepository.save(inv);
                            }
                            usedFreeze = true;
                            break;
                        }
                    }
                }
            }

            // 2. Fallback to streakFreezeCount (legacy/awarded freezes)
            if (!usedFreeze && freezeCount > 0) {
                user.setStreakFreezeCount(freezeCount - 1);
                usedFreeze = true;
            }

            if (usedFreeze) {
                newStreak = currentStreak;
            } else {
                newStreak = 1;
            }
            updated = true;
        }

        if (updated) {
            int longestStreak = Objects.requireNonNullElse(user.getLongestStreak(), 0);
            user.setCurrentStreak(newStreak);
            user.setLongestStreak(Math.max(longestStreak, newStreak));
            user.setLastStreakDate(today);

            if (newStreak >= FREEZE_AWARD_THRESHOLD && !awarded) {
                user.setStreakFreezeCount(freezeCount + 1);
                user.setStreakFreezeAwarded(true);
            }

            if (newStreak == 1 && currentStreak >= FREEZE_AWARD_THRESHOLD) {
                user.setStreakFreezeAwarded(false);
            }

            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public User getUserForStreak(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    /**
     * Mua thêm 1 lượt Streak Freeze bằng coin (sink quan trọng nhất trong nền kinh tế:
     * bảo vệ streak - "tài sản cảm xúc" lớn nhất của user).
     */
    @Transactional
    public User buyStreakFreeze(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        int coins = Objects.requireNonNullElse(user.getCoins(), 0);
        if (coins < FREEZE_COST_COINS) {
            throw new InsufficientCoinsException(
                    "Khong du coins. Can " + FREEZE_COST_COINS + ", hien co " + coins);
        }

        int freezeCount = Objects.requireNonNullElse(user.getStreakFreezeCount(), 0);
        user.setCoins(coins - FREEZE_COST_COINS);
        user.setStreakFreezeCount(freezeCount + 1);
        userRepository.save(user);

        coinTransactionRepository.save(CoinTransaction.builder()
                .userId(userId)
                .amount(-FREEZE_COST_COINS)
                .transactionType(TransactionType.BUY_STREAK_FREEZE)
                .build());

        return user;
    }
}
