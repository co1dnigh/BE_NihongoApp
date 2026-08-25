package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.RoadmapLessonResponse.Status;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.Topic;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.UserLessonProgress.ProgressStatus;
import com.example.nihongo_app.repository.TopicRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

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
 *   <li>Bài TOPIC_REVIEW (node "ôn tập" bắt buộc, câu hỏi lấy từ topic cũ):
 *     <ul>
 *       <li>UNLOCKED theo đúng quy tắc của NORMAL bên dưới (dựa vào bài NORMAL liền trước).</li>
 *       <li>Hoàn thành nó là ĐIỀU KIỆN BẮT BUỘC để bài NORMAL ngay sau nó (nếu có) được
 *           UNLOCKED — đây là cổng chặn đường kiểu Duolingo, khác TIMED_REVIEW.</li>
 *     </ul>
 *   </li>
 *   <li>Bài NORMAL:
 *     <ul>
 *       <li>Bài NORMAL đầu tiên toàn hệ thống (topic có orderIndex nhỏ nhất, bài có
 *           orderIndex nhỏ nhất) → UNLOCKED mặc định.</li>
 *       <li>Ngược lại → UNLOCKED nếu bài NORMAL ngay trước nó đã COMPLETED VÀ (nếu có
 *           TOPIC_REVIEW xen giữa) TOPIC_REVIEW đó cũng đã COMPLETED. Cờ NORMAL-completed
 *           được CARRY xuyên suốt Topic: bài đầu tiên của Topic N+1 dựa vào trạng thái bài
 *           NORMAL cuối cùng của Topic N, chứ không reset về false khi sang Topic mới.</li>
 *       <li>Còn lại → LOCKED.</li>
 *     </ul>
 *   </li>
 *   <li>Bài TIMED_REVIEW ("ôn tập tính giờ" — mascot cạnh đường đi, không bắt buộc):
 *     luôn UNLOCKED, không tham gia vào chuỗi gate ở trên.</li>
 * </ul>
 *
 * <p>Helper này chỉ <b>đọc</b> (read-only), không ghi database. Mọi thao tác ghi (upsert
 * progress, trừ năng lượng, cộng EXP) đều do service gọi helper này thực hiện.</p>
 */
@Component
@RequiredArgsConstructor
public class LessonUnlockPolicy {

    private final TopicRepository topicRepository;
    private final UserLessonProgressRepository progressRepository;

    /** Chi phi nang luong mac dinh de vao 1 bai hoc (khop voi max_energy mac dinh 25). */
    public static final int DEFAULT_ENTRY_COST_ENERGY = 10;

    /**
     * Xét trạng thái của 1 bài học bất kỳ cho 1 user.
     *
     * <p>Tính trạng thái của TOÀN BỘ lesson trong hệ thống (qua {@link #computeStatuses})
     * rồi lấy ra đúng bài cần xét, để đảm bảo cờ "bài NORMAL trước đã COMPLETED" được
     * carry xuyên suốt các Topic giống hệt {@code RoadmapServiceImpl}.</p>
     *
     * @param lesson bài học cần xét trạng thái
     * @param userId id user hiện tại
     */
    public Status evaluate(Lesson lesson, Long userId) {
        List<Topic> topics = topicRepository.findAllActiveWithLessons();

        List<UserLessonProgress> userProgresses = progressRepository.findAllByUserId(userId);
        Map<Long, UserLessonProgress> progressByLesson = new HashMap<>(userProgresses.size() * 2);
        for (UserLessonProgress p : userProgresses) {
            progressByLesson.put(p.getLessonId(), p);
        }

        Map<Long, Status> statusByLesson = computeStatuses(topics, progressByLesson);
        return statusByLesson.getOrDefault(lesson.getId(), Status.LOCKED);
    }

    /**
     * Tính trạng thái cho TẤT CẢ lesson thuộc {@code topicsOrdered} (đã sắp theo
     * {@code topic.orderIndex} rồi {@code lesson.orderIndex}, xem
     * {@link com.example.nihongo_app.repository.TopicRepository#findAllActiveWithLessons()}).
     *
     * <p>Cờ "bài NORMAL trước đã COMPLETED" được carry liên tục qua từng Topic (không
     * reset về false ở đầu mỗi Topic) — đây là điểm mấu chốt để bài đầu tiên của 1 Topic
     * mở khoá đúng khi Topic trước đó vừa hoàn thành.</p>
     */
    public Map<Long, Status> computeStatuses(List<Topic> topicsOrdered,
                                             Map<Long, UserLessonProgress> progressByLesson) {
        Map<Long, Status> result = new HashMap<>();
        // Bài NORMAL đầu tiên toàn hệ thống luôn UNLOCKED mặc định.
        boolean previousNormalCompleted = true;
        // Cong TOPIC_REVIEW: chua gap TOPIC_REVIEW nao chua qua duoc thi coi nhu da qua
        // (khong co gi de chan). Bi ha xuong false ngay khi gap 1 TOPIC_REVIEW chua
        // COMPLETED, va duoc reset lai ve true moi khi di qua 1 bai NORMAL.
        boolean pendingTopicReviewCompleted = true;

        for (Topic topic : topicsOrdered) {
            List<Lesson> sortedLessons = topic.getLessons().stream()
                    .sorted(Comparator.comparing(Lesson::getOrderIndex,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            for (Lesson lesson : sortedLessons) {
                UserLessonProgress progress = progressByLesson.get(lesson.getId());
                Status status = computeOne(lesson, previousNormalCompleted, pendingTopicReviewCompleted, progress);
                result.put(lesson.getId(), status);

                if (lesson.getLessonType() == LessonType.NORMAL) {
                    previousNormalCompleted = isCompleted(progress);
                    pendingTopicReviewCompleted = true;
                } else if (lesson.getLessonType() == LessonType.TOPIC_REVIEW) {
                    pendingTopicReviewCompleted = isCompleted(progress);
                }
            }
        }
        return result;
    }

    /**
     * Hàm lõi: tính trạng thái cho 1 lesson, với cờ "bài NORMAL trước đã COMPLETED" và
     * "TOPIC_REVIEW xen giữa đã COMPLETED" đã có sẵn (đã carry đúng qua các Topic trước đó).
     */
    public Status computeOne(Lesson lesson,
                             boolean previousNormalCompleted,
                             boolean pendingTopicReviewCompleted,
                             UserLessonProgress progress) {
        if (isCompleted(progress)) {
            return Status.COMPLETED;
        }
        if (lesson.getLessonType() == LessonType.JUMP_TEST
                || lesson.getLessonType() == LessonType.TIMED_REVIEW) {
            return Status.UNLOCKED;
        }
        if (lesson.getLessonType() == LessonType.TOPIC_REVIEW) {
            return previousNormalCompleted ? Status.UNLOCKED : Status.LOCKED;
        }
        return (previousNormalCompleted && pendingTopicReviewCompleted) ? Status.UNLOCKED : Status.LOCKED;
    }

    /**
     * Tiện ích: tính số sao hiển thị cho 1 lesson.
     * TOPIC_REVIEW / TIMED_REVIEW → lấy từ progress (cả hai đều tính sao theo
     * thời gian làm bài, xem {@code LessonAttemptServiceImpl.resolveStars}).
     * Các loại khác (NORMAL, JUMP_TEST) → 0.
     */
    public int computeStars(Lesson lesson, UserLessonProgress progress) {
        if (progress == null) {
            return 0;
        }
        boolean scoredByStars = lesson.getLessonType() == LessonType.TOPIC_REVIEW
                || lesson.getLessonType() == LessonType.TIMED_REVIEW;
        if (!scoredByStars) {
            return 0;
        }
        return Objects.requireNonNullElse(progress.getStarsEarned(), 0);
    }

    /**
     * Chi phi nang luong de vao 1 bai hoc: {@code configJson.entryCostEnergy} neu co,
     * khong thi {@value #DEFAULT_ENTRY_COST_ENERGY}.
     *
     * <p>Dat o day (thay vi rieng trong LessonAttemptService) vi ca man ban do cung
     * can con so nay de bao truoc cho nguoi hoc "bai nay ton bao nhieu nang luong".
     * Hai noi tinh ra hai ket qua khac nhau la kieu loi UI noi doi kho lan ra nhat.</p>
     *
     * <p>{@code static} co chu dich: ham thuan, khong dung repository nao, va de o dang
     * static thi khong bi mock che mat trong unit test cua cac service goi no.</p>
     */
    public static int computeEntryCost(Lesson lesson) {
        JsonNode config = lesson.getConfigJson();
        if (config != null && config.has("entryCostEnergy")) {
            return config.get("entryCostEnergy").asInt();
        }
        return DEFAULT_ENTRY_COST_ENERGY;
    }

    private boolean isCompleted(UserLessonProgress progress) {
        return progress != null && progress.getStatus() == ProgressStatus.COMPLETED;
    }
}