package com.example.nihongo_app.service;

import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserRepository userRepository;
    private static final int FREEZE_AWARD_THRESHOLD = 10;

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
            if (freezeCount > 0) {
                newStreak = currentStreak;
                user.setStreakFreezeCount(freezeCount - 1);
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
}
