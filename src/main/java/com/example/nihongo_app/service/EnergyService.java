package com.example.nihongo_app.service;

import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.exception.InsufficientCoinsException;
import com.example.nihongo_app.exception.InsufficientEnergyException;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class EnergyService {

    private final UserRepository userRepository;
    private static final int MAX_ENERGY = 5;
    private static final int REFILL_COST_COINS = 400;
    private static final int RECOVERY_INTERVAL_SECONDS = 3600;
    private static final int AD_COOLDOWN_SECONDS = 1800;
    private static final int AD_REWARD_ENERGY = 5;

    /**
     * Hoi nang luong thu dong: +1 nang luong moi {@value #RECOVERY_INTERVAL_SECONDS} giay
     * troi qua (thay vi +1/ngay nhu truoc), cap o maxEnergy. Phan du (chua du 1 gio) duoc
     * giu lai bang cach chi tien "last reset" len dung so giay da tinh, khong reset ve "now".
     */
    @Transactional
    public void recoverEnergy(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = user.getLastEnergyResetDate();

        if (lastReset == null) {
            user.setLastEnergyResetDate(now);
            userRepository.save(user);
            return;
        }

        long elapsedSeconds = Duration.between(lastReset, now).getSeconds();
        int recoveryCount = (int) (elapsedSeconds / RECOVERY_INTERVAL_SECONDS);

        if (recoveryCount > 0) {
            int currentEnergy = Objects.requireNonNullElse(user.getCurrentEnergy(), 0);
            int maxEnergy = Objects.requireNonNullElse(user.getMaxEnergy(), MAX_ENERGY);

            if (currentEnergy < maxEnergy) {
                int newEnergy = Math.min(currentEnergy + recoveryCount, maxEnergy);
                user.setCurrentEnergy(newEnergy);
            }

            user.setLastEnergyResetDate(now.minusSeconds(elapsedSeconds % RECOVERY_INTERVAL_SECONDS));
            userRepository.save(user);
        }
    }

    /**
     * Hoi {@value #AD_REWARD_ENERGY} nang luong bang cach xem quang cao (rewarded ad),
     * cach nhau toi thieu {@value #AD_COOLDOWN_SECONDS} giay giua 2 lan.
     */
    @Transactional
    public void watchAd(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        recoverEnergy(userId);

        int maxEnergy = Objects.requireNonNullElse(user.getMaxEnergy(), MAX_ENERGY);
        int currentEnergy = Objects.requireNonNullElse(user.getCurrentEnergy(), 0);
        if (currentEnergy >= maxEnergy) {
            throw new IllegalStateException("Nang luong da day, khong can xem quang cao");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAdWatch = user.getLastAdWatchDate();
        if (lastAdWatch != null) {
            long elapsedSeconds = Duration.between(lastAdWatch, now).getSeconds();
            if (elapsedSeconds < AD_COOLDOWN_SECONDS) {
                throw new IllegalStateException(
                        "Vui long cho " + ((AD_COOLDOWN_SECONDS - elapsedSeconds) / 60)
                                + " phut de xem quang cao lan nua");
            }
        }

        user.setCurrentEnergy(Math.min(currentEnergy + AD_REWARD_ENERGY, maxEnergy));
        user.setLastAdWatchDate(now);
        userRepository.save(user);
    }

    @Transactional
    public int deductEnergy(Long userId, int amount) {
        if (amount <= 0) return 0;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        recoverEnergy(userId);

        int currentEnergy = Objects.requireNonNullElse(user.getCurrentEnergy(), 0);
        if (currentEnergy < amount) {
            throw new InsufficientEnergyException(
                    "Khong du nang luong. Can " + amount + ", hien co " + currentEnergy);
        }

        user.setCurrentEnergy(currentEnergy - amount);
        userRepository.save(user);
        return currentEnergy - amount;
    }

    @Transactional
    public void refillWithCoins(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        recoverEnergy(userId);

        int maxEnergy = Objects.requireNonNullElse(user.getMaxEnergy(), MAX_ENERGY);
        int currentEnergy = Objects.requireNonNullElse(user.getCurrentEnergy(), 0);

        if (currentEnergy >= maxEnergy) {
            throw new IllegalStateException("Nang luong da day, khong can nap");
        }

        int coins = Objects.requireNonNullElse(user.getCoins(), 0);
        if (coins < REFILL_COST_COINS) {
            throw new InsufficientCoinsException(
                    "Khong du coins. Can " + REFILL_COST_COINS + ", hien co " + coins);
        }

        user.setCoins(coins - REFILL_COST_COINS);
        user.setCurrentEnergy(maxEnergy);
        userRepository.save(user);
    }

    @Transactional
    public void addEnergy(Long userId, int amount) {
        if (amount <= 0) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        int maxEnergy = Objects.requireNonNullElse(user.getMaxEnergy(), MAX_ENERGY);
        int currentEnergy = Objects.requireNonNullElse(user.getCurrentEnergy(), 0);

        user.setCurrentEnergy(Math.min(currentEnergy + amount, maxEnergy));
        userRepository.save(user);
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW)
    public User getUserForRead(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
