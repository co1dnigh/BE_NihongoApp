package com.example.nihongo_app.service;

import com.example.nihongo_app.entity.QuestDefinition;
import com.example.nihongo_app.entity.QuestDefinition.QuestType;
import com.example.nihongo_app.entity.UserDailyQuest;
import com.example.nihongo_app.repository.QuestDefinitionRepository;
import com.example.nihongo_app.repository.UserDailyQuestRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quản lý Daily Quest: mỗi user mỗi ngày được gán ngẫu nhiên 3 quest từ
 * {@link QuestDefinition} đang active. Hoàn thành đủ 3 quest là điều kiện để
 * mở {@code ChestService} (Rương thưởng hàng ngày).
 *
 * <p>Quest được gán "lười" (lazy): không cần job chạy nền lúc nửa đêm, chỉ tạo khi
 * có request đầu tiên trong ngày (gọi {@link #ensureTodayQuests}).</p>
 */
@Service
@RequiredArgsConstructor
public class DailyQuestService {

    private static final int QUESTS_PER_DAY = 3;

    private final QuestDefinitionRepository questDefinitionRepository;
    private final UserDailyQuestRepository userDailyQuestRepository;

    /**
     * Đảm bảo user đã có đủ {@value #QUESTS_PER_DAY} quest cho hôm nay, tạo mới nếu chưa có.
     */
    @Transactional
    public List<UserDailyQuest> ensureTodayQuests(Long userId) {
        LocalDate today = LocalDate.now();
        List<UserDailyQuest> existing = userDailyQuestRepository.findAllByUserIdAndQuestDate(userId, today);
        if (!existing.isEmpty()) {
            return existing;
        }

        List<QuestDefinition> pool = new ArrayList<>(questDefinitionRepository.findAllByActiveTrue());
        Collections.shuffle(pool);
        int pickCount = Math.min(QUESTS_PER_DAY, pool.size());

        List<UserDailyQuest> assigned = new ArrayList<>(pickCount);
        for (int i = 0; i < pickCount; i++) {
            QuestDefinition definition = pool.get(i);
            assigned.add(userDailyQuestRepository.save(UserDailyQuest.builder()
                    .userId(userId)
                    .questDefinitionId(definition.getId())
                    .questDate(today)
                    .title(definition.getTitle())
                    .questType(definition.getQuestType())
                    .targetValue(definition.getTargetValue())
                    .currentProgress(0)
                    .completed(false)
                    .build()));
        }
        return assigned;
    }

    /**
     * Lấy danh sách quest hôm nay (tự tạo nếu chưa có).
     */
    @Transactional
    public List<UserDailyQuest> getTodayQuests(Long userId) {
        return ensureTodayQuests(userId);
    }

    /**
     * Cộng dồn tiến độ cho các quest hôm nay khớp {@code type} và chưa hoàn thành.
     * Được gọi từ {@code LessonAttemptServiceImpl.submitLesson()} sau mỗi lần pass.
     */
    @Transactional
    public void recordProgress(Long userId, QuestType type, int amount) {
        if (amount <= 0) {
            return;
        }
        List<UserDailyQuest> todayQuests = ensureTodayQuests(userId);
        for (UserDailyQuest quest : todayQuests) {
            if (quest.getQuestType() != type || Boolean.TRUE.equals(quest.getCompleted())) {
                continue;
            }
            int newProgress = Math.min(quest.getCurrentProgress() + amount, quest.getTargetValue());
            quest.setCurrentProgress(newProgress);
            if (newProgress >= quest.getTargetValue()) {
                quest.setCompleted(true);
                quest.setCompletedAt(LocalDateTime.now());
            }
            userDailyQuestRepository.save(quest);
        }
    }

    /**
     * {@code true} nếu tất cả quest hôm nay đều đã hoàn thành (điều kiện mở rương).
     */
    @Transactional(readOnly = true)
    public boolean areAllTodayQuestsCompleted(Long userId) {
        List<UserDailyQuest> todayQuests = userDailyQuestRepository
                .findAllByUserIdAndQuestDate(userId, LocalDate.now());
        return !todayQuests.isEmpty() && todayQuests.stream()
                .allMatch(q -> Boolean.TRUE.equals(q.getCompleted()));
    }
}
