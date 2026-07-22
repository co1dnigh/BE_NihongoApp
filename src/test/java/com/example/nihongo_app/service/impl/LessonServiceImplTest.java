package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.request.LessonRequest;
import com.example.nihongo_app.dto.response.LessonDetailResponse;
import com.example.nihongo_app.dto.response.LessonSummaryResponse;
import com.example.nihongo_app.entity.Exercise;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.Vocabulary;
import com.example.nihongo_app.repository.ExerciseRepository;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.repository.VocabularyRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    private static final String EMAIL = "learner@test.com";

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private VocabularyRepository vocabularyRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private UserLessonProgressRepository userLessonProgressRepository;
    @Mock
    private UserRepository userRepository;

    private LessonServiceImpl lessonService;
    private User currentUser;

    @BeforeEach
    void setUp() {
        lessonService = new LessonServiceImpl(lessonRepository, vocabularyRepository, exerciseRepository,
                userLessonProgressRepository, userRepository, new ObjectMapper());
        currentUser = User.builder().id(10L).email(EMAIL).role("LEARNER").build();
    }

    private Lesson lesson(long id, int orderIndex) {
        return Lesson.builder().id(id).jlptLevel("N5").title("Lesson " + id).orderIndex(orderIndex).build();
    }

    @Test
    void getLessonsByLevel_firstLessonUnlockedByDefault() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        List<Lesson> lessons = List.of(lesson(1L, 1), lesson(2L, 2));
        when(lessonRepository.findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc("N5")).thenReturn(lessons);
        when(userLessonProgressRepository.findByUserIdAndLessonIdIn(10L, List.of(1L, 2L))).thenReturn(List.of());

        List<LessonSummaryResponse> result = lessonService.getLessonsByLevel(EMAIL, "N5");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.get(1).getStatus()).isEqualTo("LOCKED");
    }

    @Test
    void getLessonsByLevel_nextLessonUnlocksWhenPreviousCompleted() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        List<Lesson> lessons = List.of(lesson(1L, 1), lesson(2L, 2));
        when(lessonRepository.findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc("N5")).thenReturn(lessons);
        UserLessonProgress completedProgress = UserLessonProgress.builder()
                .id(1L).userId(10L).lessonId(1L).status("COMPLETED").build();
        when(userLessonProgressRepository.findByUserIdAndLessonIdIn(10L, List.of(1L, 2L)))
                .thenReturn(List.of(completedProgress));

        List<LessonSummaryResponse> result = lessonService.getLessonsByLevel(EMAIL, "N5");

        assertThat(result.get(0).getStatus()).isEqualTo("COMPLETED");
        assertThat(result.get(1).getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void getLessonDetail_throwsForbidden_whenLocked() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        Lesson lesson2 = lesson(2L, 2);
        when(lessonRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(lesson2));
        when(lessonRepository.findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc("N5"))
                .thenReturn(List.of(lesson(1L, 1), lesson2));
        when(userLessonProgressRepository.findByUserIdAndLessonIdIn(10L, List.of(1L, 2L))).thenReturn(List.of());

        assertThatThrownBy(() -> lessonService.getLessonDetail(EMAIL, 2L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(userLessonProgressRepository, never()).save(any());
    }

    @Test
    void getLessonDetail_throwsNotFound_whenLessonMissing() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        when(lessonRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.getLessonDetail(EMAIL, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getLessonDetail_success_createsProgressRow_hidesCorrectAnswer() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        Lesson lesson1 = lesson(1L, 1);
        when(lessonRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(lesson1));
        when(lessonRepository.findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc("N5"))
                .thenReturn(List.of(lesson1));
        when(userLessonProgressRepository.findByUserIdAndLessonIdIn(10L, List.of(1L))).thenReturn(List.of());
        when(userLessonProgressRepository.findByUserIdAndLessonId(10L, 1L)).thenReturn(Optional.empty());
        when(userLessonProgressRepository.save(any(UserLessonProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Vocabulary vocabulary = Vocabulary.builder().id(1L).lessonId(1L).kanji("こんにちは").meaningVn("Xin chao").build();
        when(vocabularyRepository.findByLessonIdAndDeletedAtIsNullOrderByIdAsc(1L)).thenReturn(List.of(vocabulary));

        Exercise exercise = Exercise.builder().id(1L).lessonId(1L).exerciseType("MULTIPLE_CHOICE")
                .questionText("Q?").correctAnswer("A").optionsJson("[\"A\",\"B\"]").build();
        when(exerciseRepository.findByLessonIdOrderByIdAsc(1L)).thenReturn(List.of(exercise));

        LessonDetailResponse response = lessonService.getLessonDetail(EMAIL, 1L);

        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.getVocabularies()).hasSize(1);
        assertThat(response.getExercises()).hasSize(1);
        assertThat(response.getExercises().get(0).getOptions()).containsExactly("A", "B");

        ArgumentCaptor<UserLessonProgress> captor = ArgumentCaptor.forClass(UserLessonProgress.class);
        verify(userLessonProgressRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void completeLesson_marksExistingProgressAsCompleted() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        Lesson lesson1 = lesson(1L, 1);
        when(lessonRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(lesson1));
        when(lessonRepository.findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc("N5"))
                .thenReturn(List.of(lesson1));
        when(userLessonProgressRepository.findByUserIdAndLessonIdIn(10L, List.of(1L))).thenReturn(List.of());
        UserLessonProgress existing = UserLessonProgress.builder()
                .id(5L).userId(10L).lessonId(1L).status("IN_PROGRESS").build();
        when(userLessonProgressRepository.findByUserIdAndLessonId(10L, 1L)).thenReturn(Optional.of(existing));

        lessonService.completeLesson(EMAIL, 1L);

        ArgumentCaptor<UserLessonProgress> captor = ArgumentCaptor.forClass(UserLessonProgress.class);
        verify(userLessonProgressRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("COMPLETED");
        assertThat(captor.getValue().getId()).isEqualTo(5L);
    }

    @Test
    void completeLesson_throwsForbidden_whenLocked() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        Lesson lesson2 = lesson(2L, 2);
        when(lessonRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(lesson2));
        when(lessonRepository.findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc("N5"))
                .thenReturn(List.of(lesson(1L, 1), lesson2));
        when(userLessonProgressRepository.findByUserIdAndLessonIdIn(10L, List.of(1L, 2L))).thenReturn(List.of());

        assertThatThrownBy(() -> lessonService.completeLesson(EMAIL, 2L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(userLessonProgressRepository, never()).save(any());
    }

    @Test
    void createLesson_savesMappedEntity() {
        LessonRequest request = LessonRequest.builder().jlptLevel("N4").title("Bai moi").orderIndex(3).build();
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        var response = lessonService.createLesson(request);

        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getJlptLevel()).isEqualTo("N4");
        assertThat(response.getOrderIndex()).isEqualTo(3);
    }

    @Test
    void updateLesson_throwsNotFound_whenMissing() {
        when(lessonRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        LessonRequest request = LessonRequest.builder().jlptLevel("N5").title("x").orderIndex(1).build();

        assertThatThrownBy(() -> lessonService.updateLesson(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void deleteLesson_softDeletesBySettingDeletedAt() {
        Lesson lesson1 = lesson(1L, 1);
        when(lessonRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(lesson1));

        lessonService.deleteLesson(1L);

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }
}
