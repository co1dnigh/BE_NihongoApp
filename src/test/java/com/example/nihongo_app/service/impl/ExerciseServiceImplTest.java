package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.request.ExerciseRequest;
import com.example.nihongo_app.dto.request.SubmitAnswerRequest;
import com.example.nihongo_app.dto.response.ExerciseAdminResponse;
import com.example.nihongo_app.dto.response.SubmitAnswerResponse;
import com.example.nihongo_app.entity.Exercise;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.repository.ExerciseRepository;
import com.example.nihongo_app.repository.LessonRepository;
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
class ExerciseServiceImplTest {

    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private LessonRepository lessonRepository;

    private ExerciseServiceImpl exerciseService;

    @BeforeEach
    void setUp() {
        exerciseService = new ExerciseServiceImpl(exerciseRepository, lessonRepository, new ObjectMapper());
    }

    private ExerciseRequest sampleRequest() {
        return ExerciseRequest.builder()
                .lessonId(1L)
                .exerciseType("MULTIPLE_CHOICE")
                .questionText("Xin chao la gi?")
                .correctAnswer("Konnichiwa")
                .options(List.of("Konnichiwa", "Sayonara"))
                .build();
    }

    @Test
    void createExercise_throwsNotFound_whenLessonMissing() {
        when(lessonRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.createExercise(sampleRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void createExercise_serializesOptionsToJsonAndBack() {
        when(lessonRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(Lesson.builder().id(1L).build()));
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> {
            Exercise saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        ExerciseAdminResponse response = exerciseService.createExercise(sampleRequest());

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getCorrectAnswer()).isEqualTo("Konnichiwa");
        assertThat(response.getOptions()).containsExactly("Konnichiwa", "Sayonara");

        ArgumentCaptor<Exercise> captor = ArgumentCaptor.forClass(Exercise.class);
        verify(exerciseRepository).save(captor.capture());
        assertThat(captor.getValue().getOptionsJson()).contains("Konnichiwa").contains("Sayonara");
    }

    @Test
    void submitAnswer_correct_isCaseInsensitive() {
        Exercise exercise = Exercise.builder().id(1L).lessonId(1L).correctAnswer("Konnichiwa").build();
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        SubmitAnswerResponse response = exerciseService.submitAnswer("learner@test.com", 1L,
                SubmitAnswerRequest.builder().answer(" konnichiwa ").build());

        assertThat(response.isCorrect()).isTrue();
        assertThat(response.getCorrectAnswer()).isEqualTo("Konnichiwa");
    }

    @Test
    void submitAnswer_incorrect() {
        Exercise exercise = Exercise.builder().id(1L).lessonId(1L).correctAnswer("Konnichiwa").build();
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        SubmitAnswerResponse response = exerciseService.submitAnswer("learner@test.com", 1L,
                SubmitAnswerRequest.builder().answer("Sayonara").build());

        assertThat(response.isCorrect()).isFalse();
        assertThat(response.getCorrectAnswer()).isEqualTo("Konnichiwa");
    }

    @Test
    void submitAnswer_throwsNotFound_whenExerciseMissing() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.submitAnswer("learner@test.com", 99L,
                SubmitAnswerRequest.builder().answer("x").build()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteExercise_hardDeletes() {
        Exercise exercise = Exercise.builder().id(1L).lessonId(1L).build();
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        exerciseService.deleteExercise(1L);

        verify(exerciseRepository).delete(exercise);
        verify(exerciseRepository, never()).save(any());
    }
}
