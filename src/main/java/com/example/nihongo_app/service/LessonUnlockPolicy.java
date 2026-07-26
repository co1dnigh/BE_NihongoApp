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
 *
 * <p>Mục đích: cả {@code RoadmapService} (vẽ bản đồ) và {@code LessonAttemptService}
 * (API /start, /submit, /cancel) đều phải ra cùng một quyết định cho một bài học.
 * Gom logic vào đây để tránh duplicate code và đảm bảo trước-sau nhất quán.</p>
 *
 * <h3>Thuật toán</h3>
 * <ul>
 *   <li>Bài đã có {@code user_lesson_progress.status = COMPLETED} → COMPLETED.</li>
 *   <li>Bài {@code JUMP_TEST} → luôn UNLOCKED.</li>
 *   <li>Bài NORMAL / TIMED_REVIEW:
 *     <ul>
 *       <li>Bài NORMAL đầu tiên của Topic đầu tiên (orderIndex nhỏ nhất toàn hệ thống)
 *           → UNLOCKED mặc định.</li>
 *       <li>Ngược lại → UNLOCKED nếu bài NORMAL ngay trước nó đã COMPLETED.</li>
 *       <li>Còn lại → LOCKED.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>Helper này chỉ <b>đọc</b> (read-only), không ghi database. Mọi thao tác ghi (upsert
 * progress, trừ năng lượng, cộng EXP) đều do service gọi helper này thực hiện.</p>
 */
@Component
@RequiredArgsConstructor
public class LessonUnlockPolicy {

    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository progressRepository;

    /**
     * Xét trạng thái của 1 bài học bất kỳ cho 1 user.
     *
     * <p>Thực hiện 2 query:
     * <ol>
     *   <li>Lấy toàn bộ lesson thuộc cùng topic với {@code lesson}, sắp xếp theo orderIndex.</li>
     *   <li>Lấy toàn bộ progress của user (1 query duy nhất).</li>
     * </ol>
     *
     * <p>Phù hợp với các tình huống chỉ cần xét 1 bài (ví dụ API /start).</p>
     *
     * @param lesson bài học cần xét trạng thái
     * @param userId id user hiện tại
     * @param isFirstTopic {@code true} nếu bài nằm trong topic có orderIndex nhỏ nhất
     *                    toàn hệ thống (service gọi cần xác định flag này).
     */
    public Status evaluate(Lesson lesson, Long userId, boolean isFirstTopic) {
        List<Lesson> siblings = lessonRepository.findAllByTopicIdOrdered(lesson.getTopicId());

        List<UserLessonProgress> userProgresses = progressRepository.findAllByUserId(userId);
        Map<Long, UserLessonProgress> progressByLesson = new HashMap<>(userProgresses.size() * 2);
        for (UserLessonProgress p : userProgresses) {
            progressByLesson.put(p.getLessonId(), p);
        }

        boolean previousNormalCompleted = isFirstTopic && isFirstLessonOfTopic(siblings);
        Status result = Status.LOCKED;

        for (Lesson sibling : siblings) {
            UserLessonProgress progress = progressByLesson.get(sibling.getId());
            Status status = computeOne(sibling, isFirstTopic, previousNormalCompleted, progress);

            if (Objects.equals(sibling.getId(), lesson.getId())) {
                result = status;
                break;
            }

            if (sibling.getLessonType() == LessonType.NORMAL) {
                previousNormalCompleted = isCompleted(progress);
            }
        }
        return result;
    }

    /**
     * Hàm lõi: tính trạng thái cho 1 lesson, với cờ "bài NORMAL trước đã COMPLETED"
     * đã có sẵn. Dùng cho {@code RoadmapServiceImpl} để khỏi truy vấn lại database
     * (giữ nguyên hiệu năng vẽ bản đồ).
     */
    public Status computeOne(Lesson lesson,
                             boolean isFirstTopic,
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

    /**
     * Trong topic hiện tại, bài đầu tiên (theo orderIndex) có phải là bài NORMAL không?
     * Nếu đúng → bài đầu của topic đầu hệ thống được UNLOCKED mặc định.
     */
    private boolean isFirstLessonOfTopic(List<Lesson> sortedSiblings) {
        if (sortedSiblings.isEmpty()) {
            return false;
        }
        Lesson first = sortedSiblings.get(0);
        return first.getLessonType() == LessonType.NORMAL;
    }
}