package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.response.RoadmapLessonResponse;
import com.example.nihongo_app.dto.response.RoadmapLessonResponse.Status;
import com.example.nihongo_app.dto.response.RoadmapTopicResponse;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Topic;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.repository.TopicRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import com.example.nihongo_app.service.LessonUnlockPolicy;
import com.example.nihongo_app.service.RoadmapService;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tính toán lộ trình học cho 1 user.
 *
 * <h3>Chiến lược tải dữ liệu (tránh N+1):</h3>
 * <ul>
 *   <li>1 query JPQL {@code JOIN FETCH} lấy toàn bộ Topic + Lesson đang active.</li>
 *   <li>1 query lấy toàn bộ {@code user_lesson_progress} của user, chuyển thành
 *       {@code Map<Long lessonId, UserLessonProgress>} để tra cứu O(1) trên RAM.</li>
 * </ul>
 *
 * <p>Logic xét trạng thái từng bài học đã được tách sang {@link LessonUnlockPolicy}
 * để chia sẻ với {@code LessonAttemptService} (Start/Submit/Cancel).</p>
 */
@Service
@RequiredArgsConstructor
public class RoadmapServiceImpl implements RoadmapService {

    private final TopicRepository topicRepository;
    private final UserLessonProgressRepository progressRepository;
    private final LessonUnlockPolicy unlockPolicy;

    @Override
    @Transactional(readOnly = true)
    public List<RoadmapTopicResponse> getRoadmap(Long userId) {
        List<Topic> topics = topicRepository.findAllActiveWithLessons();

        // 1 query duy nhất lấy toàn bộ lịch sử của user → Map<lessonId, progress> (O(1)).
        List<UserLessonProgress> progresses = progressRepository.findAllByUserId(userId);
        Map<Long, UserLessonProgress> progressByLesson = new HashMap<>(progresses.size() * 2);
        for (UserLessonProgress p : progresses) {
            progressByLesson.put(p.getLessonId(), p);
        }

        // Tính status cho TẤT CẢ lesson 1 lần duy nhất: cờ "bài NORMAL trước đã
        // COMPLETED" được carry xuyên suốt các Topic (topics đã sắp theo orderIndex).
        Map<Long, Status> statusByLesson = unlockPolicy.computeStatuses(topics, progressByLesson);

        return topics.stream()
                .map(topic -> toTopicResponse(topic, statusByLesson, progressByLesson))
                .toList();
    }

    private RoadmapTopicResponse toTopicResponse(Topic topic,
                                                 Map<Long, Status> statusByLesson,
                                                 Map<Long, UserLessonProgress> progressByLesson) {
        List<Lesson> sortedLessons = topic.getLessons().stream()
                .sorted(Comparator.comparing(Lesson::getOrderIndex,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<RoadmapLessonResponse> lessonResponses = sortedLessons.stream()
                .map(lesson -> {
                    UserLessonProgress progress = progressByLesson.get(lesson.getId());
                    Status status = statusByLesson.get(lesson.getId());
                    Integer starsEarned = unlockPolicy.computeStars(lesson, progress);

                    return RoadmapLessonResponse.builder()
                            .lessonId(lesson.getId())
                            .title(lesson.getTitle())
                            .lessonType(lesson.getLessonType())
                            .orderIndex(lesson.getOrderIndex())
                            .status(status)
                            .starsEarned(starsEarned)
                            .build();
                })
                .toList();

        return RoadmapTopicResponse.builder()
                .topicId(topic.getId())
                .topicTitle(topic.getTitle())
                .lessons(lessonResponses)
                .build();
    }
}