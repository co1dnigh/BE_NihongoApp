package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.response.RoadmapLessonResponse;
import com.example.nihongo_app.dto.response.RoadmapLessonResponse.Status;
import com.example.nihongo_app.dto.response.RoadmapTopicResponse;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.Topic;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.UserLessonProgress.ProgressStatus;
import com.example.nihongo_app.repository.TopicRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import com.example.nihongo_app.service.LessonUnlockPolicy;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test {@link RoadmapServiceImpl} bằng {@link LessonUnlockPolicy} THẬT (không mock)
 * để bảo vệ đúng kịch bản bug gốc ở mức API {@code GET /api/v1/topics}: hoàn thành
 * bài cuối cùng của Topic 1 phải mở khoá bài đầu tiên của Topic 2.
 */
@ExtendWith(MockitoExtension.class)
class RoadmapServiceImplTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserLessonProgressRepository progressRepository;

    private RoadmapServiceImpl roadmapService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // LessonUnlockPolicy that cung dung topicRepository/progressRepository nhung
        // computeStatuses/computeStars khong goi lai repository nao ca (pure function).
        LessonUnlockPolicy unlockPolicy = new LessonUnlockPolicy(topicRepository, progressRepository);
        roadmapService = new RoadmapServiceImpl(topicRepository, progressRepository, unlockPolicy);
    }

    private Lesson lesson(long id, int orderIndex, LessonType type) {
        return Lesson.builder().id(id).topicId(0L).title("Lesson " + id)
                .orderIndex(orderIndex).lessonType(type).build();
    }

    private Topic topic(long id, int orderIndex, String title, Lesson... lessons) {
        return Topic.builder().id(id).title(title).orderIndex(orderIndex).lessons(List.of(lessons)).build();
    }

    private UserLessonProgress completed(long lessonId) {
        return UserLessonProgress.builder().lessonId(lessonId).status(ProgressStatus.COMPLETED).build();
    }

    private RoadmapLessonResponse lessonInTopic(RoadmapTopicResponse topicResponse, long lessonId) {
        return topicResponse.getLessons().stream()
                .filter(l -> l.getLessonId().equals(lessonId))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void getRoadmap_completingLastLessonOfTopic1_unlocksFirstLessonOfTopic2() {
        Topic topic1 = topic(1L, 1, "Topic 1", lesson(1L, 1, LessonType.NORMAL));
        Topic topic2 = topic(2L, 2, "Topic 2", lesson(2L, 1, LessonType.NORMAL));

        when(topicRepository.findAllActiveWithLessons()).thenReturn(List.of(topic1, topic2));
        when(progressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(completed(1L)));

        List<RoadmapTopicResponse> roadmap = roadmapService.getRoadmap(USER_ID);

        RoadmapTopicResponse topic1Response = roadmap.get(0);
        RoadmapTopicResponse topic2Response = roadmap.get(1);
        assertThat(lessonInTopic(topic1Response, 1L).getStatus()).isEqualTo(Status.COMPLETED);
        // Day chinh la kich ban bug goc: truoc fix, bai nay se bi LOCKED nham.
        assertThat(lessonInTopic(topic2Response, 2L).getStatus()).isEqualTo(Status.UNLOCKED);
    }

    @Test
    void getRoadmap_topic1NotCompleted_topic2FirstLessonStaysLocked() {
        Topic topic1 = topic(1L, 1, "Topic 1", lesson(1L, 1, LessonType.NORMAL));
        Topic topic2 = topic(2L, 2, "Topic 2", lesson(2L, 1, LessonType.NORMAL));

        when(topicRepository.findAllActiveWithLessons()).thenReturn(List.of(topic1, topic2));
        when(progressRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        List<RoadmapTopicResponse> roadmap = roadmapService.getRoadmap(USER_ID);

        assertThat(lessonInTopic(roadmap.get(0), 1L).getStatus()).isEqualTo(Status.UNLOCKED);
        assertThat(lessonInTopic(roadmap.get(1), 2L).getStatus()).isEqualTo(Status.LOCKED);
    }

    @Test
    void getRoadmap_mapsTopicAndLessonFields_sortedByOrderIndex() {
        Lesson second = lesson(2L, 2, LessonType.NORMAL);
        Lesson first = lesson(1L, 1, LessonType.NORMAL);
        // Co tinh dua vao topic theo thu tu "second, first" de kiem tra co sap xep lai khong.
        Topic topic = topic(1L, 1, "Chao hoi", second, first);

        when(topicRepository.findAllActiveWithLessons()).thenReturn(List.of(topic));
        when(progressRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        List<RoadmapTopicResponse> roadmap = roadmapService.getRoadmap(USER_ID);

        RoadmapTopicResponse topicResponse = roadmap.get(0);
        assertThat(topicResponse.getTopicId()).isEqualTo(1L);
        assertThat(topicResponse.getTopicTitle()).isEqualTo("Chao hoi");
        assertThat(topicResponse.getLessons()).extracting(RoadmapLessonResponse::getLessonId)
                .containsExactly(1L, 2L); // da sap theo orderIndex, khong con giu thu tu goc (2,1)
    }

    @Test
    void getRoadmap_timedReview_returnsStarsFromProgress() {
        Lesson timedReview = lesson(1L, 1, LessonType.TIMED_REVIEW);
        Topic topic = topic(1L, 1, "Topic 1", timedReview);
        UserLessonProgress progress = UserLessonProgress.builder()
                .lessonId(1L).status(ProgressStatus.COMPLETED).starsEarned(2).build();

        when(topicRepository.findAllActiveWithLessons()).thenReturn(List.of(topic));
        when(progressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(progress));

        List<RoadmapTopicResponse> roadmap = roadmapService.getRoadmap(USER_ID);

        assertThat(lessonInTopic(roadmap.get(0), 1L).getStarsEarned()).isEqualTo(2);
    }
}
