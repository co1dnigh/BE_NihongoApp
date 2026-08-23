package com.example.nihongo_app.service;

import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.CoinTransaction.TransactionType;
import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserInventory;
import com.example.nihongo_app.entity.UserStreakDay;
import com.example.nihongo_app.exception.InsufficientCoinsException;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.CoinTransactionRepository;
import com.example.nihongo_app.repository.UserInventoryRepository;
import com.example.nihongo_app.repository.UserStreakDayRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Streak (chuỗi ngày học liên tiếp) + Streak Freeze.
 *
 * <p>Timezone: dùng {@link ZoneId#of(String)} "Asia/Ho_Chi_Minh" để streak reset
 * đúng giữa đêm theo múi giờ Việt Nam (server có thể chạy UTC).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreakService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int FREEZE_AWARD_THRESHOLD = 10;
    private static final int FREEZE_COST_COINS = 200;

    private final UserRepository userRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final ShopService shopService;
    private final UserStreakDayRepository userStreakDayRepository;
    private final AchievementService achievementService;

    /** Ngày "hôm nay" theo múi giờ Việt Nam (đồng bộ cho toàn bộ streak). */
    static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    // ============================ STREAK CORE ============================

    @Transactional
    public void checkAndUpdateStreak(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        LocalDate today = today();
        LocalDate lastDate = user.getLastStreakDate();
        int currentStreak = Objects.requireNonNullElse(user.getCurrentStreak(), 0);
        int freezeCount = Objects.requireNonNullElse(user.getStreakFreezeCount(), 0);
        boolean awarded = Boolean.TRUE.equals(user.getStreakFreezeAwarded());
        int newStreak;
        boolean updated = false;
        boolean streakReset = false;
        int previousStreak = currentStreak;

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
                streakReset = true;
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

            // Ghi ngày học (idempotent theo user_id + study_date).
            recordStreakDayInternal(userId, today);

            // Emit achievement events.
            if (streakReset && previousStreak > 0) {
                achievementService.onEvent(userId,
                        AchievementProgress.of(AchievementProgress.EventType.STREAK_RESET.name(),
                                previousStreak, String.valueOf(previousStreak)));
            }
            achievementService.onEvent(userId,
                    AchievementProgress.of(AchievementProgress.EventType.STREAK_MILESTONE.name(),
                            newStreak, null));
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

        // Achievement: dùng 1 lần Streak Freeze (unlock khi user thực sự dùng).
        achievementService.onEvent(userId,
                AchievementProgress.of(AchievementProgress.EventType.STREAK_FREEZE_USED.name(),
                        1, null));

        return user;
    }

    // ============================ STREAK CALENDAR ============================

    /**
     * Ghi nhận user đã học ngày {@code studyDate} (idempotent: nếu đã có rồi thì bỏ qua).
     * Dùng để vẽ heat map trên streak calendar.
     */
    @Transactional
    public void recordStreakDay(Long userId, LocalDate studyDate) {
        recordStreakDayInternal(userId, studyDate);
    }

    private void recordStreakDayInternal(Long userId, LocalDate studyDate) {
        if (studyDate == null) {
            return;
        }
        if (userStreakDayRepository.existsByUserIdAndStudyDate(userId, studyDate)) {
            return;
        }
        userStreakDayRepository.save(UserStreakDay.builder()
                .userId(userId)
                .studyDate(studyDate)
                .build());
    }

    /**
     * Lấy lịch học N ngày gần nhất (tính từ {@link #today()} lùi về quá khứ),
     * kèm cờ đã học. Dùng để FE vẽ streak calendar / heat map.
     *
     * @param days số ngày muốn xem (tối thiểu 1)
     */
    @Transactional(readOnly = true)
    public List<LocalDate> getStreakCalendar(Long userId, int days) {
        LocalDate to = today();
        LocalDate from = to.minusDays(Math.max(1, days) - 1);
        return userStreakDayRepository
                .findAllByUserIdAndStudyDateBetween(userId, from, to).stream()
                .map(UserStreakDay::getStudyDate)
                .sorted()
                .toList();
    }

    /** Thống kê số ngày user đã học trong N ngày gần nhất. */
    @Transactional(readOnly = true)
    public int countStreakDaysInWindow(Long userId, int days) {
        LocalDate to = today();
        LocalDate from = to.minusDays(Math.max(1, days) - 1);
        return (int) userStreakDayRepository.countByUserIdAndStudyDateBetween(userId, from, to);
    }
}