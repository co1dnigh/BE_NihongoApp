package com.example.nihongo_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.response.RoadmapLessonResponse.Status;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.Topic;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.UserLessonProgress.ProgressStatus;
import com.example.nihongo_app.repository.TopicRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test cho logic mở khoá bài học ({@link LessonUnlockPolicy}), đặc biệt là bug
 * "khoá nhầm bài đầu tiên của Topic mới" đã fix trên nhánh fix/lesson-unlock-cross-topic:
 * cờ "bài NORMAL trước đã COMPLETED" phải carry xuyên suốt các Topic, không được
 * reset về false mỗi khi sang Topic mới.
 */
@ExtendWith(MockitoExtension.class)
class LessonUnlockPolicyTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserLessonProgressRepository progressRepository;

    private LessonUnlockPolicy policy;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        policy = new LessonUnlockPolicy(topicRepository, progressRepository);
    }

    private Lesson normalLesson(long id, int orderIndex) {
        return Lesson.builder().id(id).topicId(0L).title("Lesson " + id)
                .orderIndex(orderIndex).lessonType(LessonType.NORMAL).build();
    }

    private Lesson jumpTest(long id, int orderIndex) {
        return Lesson.builder().id(id).topicId(0L).title("Jump " + id)
                .orderIndex(orderIndex).lessonType(LessonType.JUMP_TEST).build();
    }

    private Topic topicWith(long id, int orderIndex, Lesson... lessons) {
        return Topic.builder().id(id).title("Topic " + id).orderIndex(orderIndex)
                .lessons(List.of(lessons)).build();
    }

    private UserLessonProgress completedProgress(long lessonId) {
        return UserLessonProgress.builder().lessonId(lessonId).status(ProgressStatus.COMPLETED).build();
    }

    private Map<Long, UserLessonProgress> progressMap(UserLessonProgress... progresses) {
        Map<Long, UserLessonProgress> map = new HashMap<>();
        for (UserLessonProgress p : progresses) {
            map.put(p.getLessonId(), p);
        }
        return map;
    }

    // ===================== computeStatuses =====================

    @Test
    void computeStatuses_firstLessonOfSystem_unlockedByDefault() {
        Topic topic = topicWith(1L, 1, normalLesson(1L, 1));

        Map<Long, Status> statuses = policy.computeStatuses(List.of(topic), Map.of());

        assertThat(statuses.get(1L)).isEqualTo(Status.UNLOCKED);
    }

    @Test
    void computeStatuses_secondLessonSameTopic_lockedWhenFirstNotCompleted() {
        Topic topic = topicWith(1L, 1, normalLesson(1L, 1), normalLesson(2L, 2));

        Map<Long, Status> statuses = policy.computeStatuses(List.of(topic), Map.of());

        assertThat(statuses.get(1L)).isEqualTo(Status.UNLOCKED);
        assertThat(statuses.get(2L)).isEqualTo(Status.LOCKED);
    }

    @Test
    void computeStatuses_secondLessonSameTopic_unlockedWhenFirstCompleted() {
        Topic topic = topicWith(1L, 1, normalLesson(1L, 1), normalLesson(2L, 2));
        Map<Long, UserLessonProgress> progress = progressMap(completedProgress(1L));

        Map<Long, Status> statuses = policy.computeStatuses(List.of(topic), progress);

        assertThat(statuses.get(1L)).isEqualTo(Status.COMPLETED);
        assertThat(statuses.get(2L)).isEqualTo(Status.UNLOCKED);
    }

    @Test
    void computeStatuses_crossTopic_firstLessonOfNextTopicUnlocked_whenLastLessonOfPreviousTopicCompleted() {
        // Dung 1 lesson/topic de mo phong dung kich ban bug goc: "hoan thanh bai cuoi Topic 1
        // -> bai dau Topic 2 phai UNLOCKED".
        Topic topic1 = topicWith(1L, 1, normalLesson(1L, 1));
        Topic topic2 = topicWith(2L, 2, normalLesson(2L, 1));
        Map<Long, UserLessonProgress> progress = progressMap(completedProgress(1L));

        Map<Long, Status> statuses = policy.computeStatuses(List.of(topic1, topic2), progress);

        assertThat(statuses.get(1L)).isEqualTo(Status.COMPLETED);
        assertThat(statuses.get(2L)).isEqualTo(Status.UNLOCKED); // truoc fix: se la LOCKED (bug)
    }

    @Test
    void computeStatuses_crossTopic_firstLessonOfNextTopicLocked_whenPreviousTopicNotCompleted() {
        Topic topic1 = topicWith(1L, 1, normalLesson(1L, 1));
        Topic topic2 = topicWith(2L, 2, normalLesson(2L, 1));

        Map<Long, Status> statuses = policy.computeStatuses(List.of(topic1, topic2), Map.of());

        assertThat(statuses.get(1L)).isEqualTo(Status.UNLOCKED); // bai dau toan he thong
        assertThat(statuses.get(2L)).isEqualTo(Status.LOCKED);   // topic1 chua xong -> topic2 khoa
    }

    @Test
    void computeStatuses_jumpTest_alwaysUnlocked_regardlessOfPreviousNormalState() {
        // lesson 1L la bai NORMAL dau tien toan he thong (UNLOCKED mac dinh) nhung CHUA hoan
        // thanh -> co "previousNormalCompleted" khi den luot lesson 2L la false. JUMP_TEST
        // van phai UNLOCKED bat chap co nay.
        Topic topic = topicWith(1L, 1, normalLesson(1L, 1), jumpTest(2L, 2));

        Map<Long, Status> statuses = policy.computeStatuses(List.of(topic), Map.of());

        assertThat(statuses.get(1L)).isEqualTo(Status.UNLOCKED);
        assertThat(statuses.get(2L)).isEqualTo(Status.UNLOCKED);
    }

    // ===================== computeOne =====================

    @Test
    void computeOne_completedProgress_returnsCompleted_regardlessOfPreviousFlag() {
        Lesson lesson = normalLesson(1L, 1);
        UserLessonProgress progress = completedProgress(1L);

        assertThat(policy.computeOne(lesson, false, progress)).isEqualTo(Status.COMPLETED);
    }

    @Test
    void computeOne_jumpTest_alwaysUnlocked() {
        Lesson lesson = jumpTest(1L, 1);

        assertThat(policy.computeOne(lesson, false, null)).isEqualTo(Status.UNLOCKED);
    }

    @Test
    void computeOne_normalLesson_unlockedOrLocked_basedOnPreviousFlag() {
        Lesson lesson = normalLesson(1L, 1);

        assertThat(policy.computeOne(lesson, true, null)).isEqualTo(Status.UNLOCKED);
        assertThat(policy.computeOne(lesson, false, null)).isEqualTo(Status.LOCKED);
    }

    // ===================== computeStars =====================

    @Test
    void computeStars_timedReview_returnsStarsFromProgress() {
        Lesson lesson = Lesson.builder().id(1L).lessonType(LessonType.TIMED_REVIEW).build();
        UserLessonProgress progress = UserLessonProgress.builder().lessonId(1L).starsEarned(3).build();

        assertThat(policy.computeStars(lesson, progress)).isEqualTo(3);
    }

    @Test
    void computeStars_nonTimedReview_returnsZero() {
        Lesson lesson = normalLesson(1L, 1);
        UserLessonProgress progress = UserLessonProgress.builder().lessonId(1L).starsEarned(3).build();

        assertThat(policy.computeStars(lesson, progress)).isZero();
    }

    @Test
    void computeStars_nullProgress_returnsZero() {
        Lesson lesson = Lesson.builder().id(1L).lessonType(LessonType.TIMED_REVIEW).build();

        assertThat(policy.computeStars(lesson, null)).isZero();
    }

    // ===================== evaluate (dung cho API /start) =====================

    @Test
    void evaluate_crossTopic_firstLessonOfNextTopicUnlocked_whenPreviousTopicCompleted() {
        Topic topic1 = topicWith(1L, 1, normalLesson(1L, 1));
        Lesson lessonInTopic2 = normalLesson(2L, 1);
        Topic topic2 = topicWith(2L, 2, lessonInTopic2);

        when(topicRepository.findAllActiveWithLessons()).thenReturn(List.of(topic1, topic2));
        when(progressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(completedProgress(1L)));

        Status status = policy.evaluate(lessonInTopic2, USER_ID);

        assertThat(status).isEqualTo(Status.UNLOCKED); // truoc fix: se la LOCKED (bug o ca API /start)
    }
}
