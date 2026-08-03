package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.RoadmapLessonResponse.Status;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.UserLessonProgress.ProgressStatus;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper dùng chung để tính trạng thái hiển thị (LOCKED / UNLOCKED / COMPLETED) của một
 * {@link Lesson} trong lộ trình học của một user.
 */
@Component
@RequiredArgsConstructor
public class LessonUnlockPolicy {

    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository progressRepository;

    /**
     * Xét trạng thái của 1 bài học bất kỳ cho 1 user.
     * Duyệt qua toàn bộ hệ thống để đảm bảo trạng thái liên tục giữa các topic.
     *
     * @param lesson bài học cần xét trạng thái
     * @param userId id user hiện tại
     */
    public Status evaluate(Lesson lesson, Long userId) {
        List<Lesson> allLessons = lessonRepository.findAllActiveOrdered();

        List<UserLessonProgress> userProgresses = progressRepository.findAllByUserId(userId);
        Map<Long, UserLessonProgress> progressByLesson = new HashMap<>(userProgresses.size() * 2);
        for (UserLessonProgress p : userProgresses) {
            progressByLesson.put(p.getLessonId(), p);
        }

        boolean previousNormalCompleted = true; // Bài NORMAL đầu tiên của hệ thống luôn được unlock
        Status result = Status.LOCKED;

        for (Lesson currentLesson : allLessons) {
            UserLessonProgress progress = progressByLesson.get(currentLesson.getId());
            Status status = computeOne(currentLesson, previousNormalCompleted, progress);

            if (Objects.equals(currentLesson.getId(), lesson.getId())) {
                result = status;
                break;
            }

            if (currentLesson.getLessonType() == LessonType.NORMAL) {
                previousNormalCompleted = isCompleted(progress);
            }
        }
        return result;
    }

    /**
     * Hàm lõi: tính trạng thái cho 1 lesson, với cờ "bài NORMAL trước đã COMPLETED"
     * đã có sẵn.
     */
    public Status computeOne(Lesson lesson,
                             boolean previousNormalCompleted,
                             UserLessonProgress progress) {
        if (isCompleted(progress)) {
            return Status.COMPLETED;
        }
        if (lesson.getLessonType() == LessonType.JUMP_TEST) {
            return Status.UNLOCKED;
        }
        return previousNormalCompleted ? Status.UNLOCKED : Status.LOCKED;
    }

    /**
     * Tiện ích: tính số sao hiển thị cho 1 lesson.
     * TIMED_REVIEW → lấy từ progress. Các loại khác → 0.
     */
    public int computeStars(Lesson lesson, UserLessonProgress progress) {
        if (progress == null) {
            return 0;
        }
        if (lesson.getLessonType() != LessonType.TIMED_REVIEW) {
            return 0;
        }
        return Objects.requireNonNullElse(progress.getStarsEarned(), 0);
    }

    private boolean isCompleted(UserLessonProgress progress) {
        return progress != null && progress.getStatus() == ProgressStatus.COMPLETED;
    }
}