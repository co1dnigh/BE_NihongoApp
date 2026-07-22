package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.ExerciseRequest;
import com.example.nihongo_app.dto.request.SubmitAnswerRequest;
import com.example.nihongo_app.dto.response.ExerciseAdminResponse;
import com.example.nihongo_app.dto.response.SubmitAnswerResponse;
import com.example.nihongo_app.entity.Exercise;
import com.example.nihongo_app.repository.ExerciseRepository;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.service.ExerciseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final LessonRepository lessonRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ExerciseAdminResponse createExercise(ExerciseRequest request) {
        lessonRepository.findByIdAndDeletedAtIsNull(request.getLessonId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        Exercise exercise = Exercise.builder()
                .lessonId(request.getLessonId())
                .exerciseType(request.getExerciseType())
                .questionText(request.getQuestionText())
                .correctAnswer(request.getCorrectAnswer())
                .optionsJson(serializeOptions(request.getOptions()))
                .build();
        return toAdminResponse(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public ExerciseAdminResponse updateExercise(Long id, ExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

        lessonRepository.findByIdAndDeletedAtIsNull(request.getLessonId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        exercise.setLessonId(request.getLessonId());
        exercise.setExerciseType(request.getExerciseType());
        exercise.setQuestionText(request.getQuestionText());
        exercise.setCorrectAnswer(request.getCorrectAnswer());
        exercise.setOptionsJson(serializeOptions(request.getOptions()));
        return toAdminResponse(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public void deleteExercise(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
        exerciseRepository.delete(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmitAnswerResponse submitAnswer(String email, Long exerciseId, SubmitAnswerRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

        boolean correct = request.getAnswer() != null
                && request.getAnswer().trim().equalsIgnoreCase(exercise.getCorrectAnswer().trim());

        return SubmitAnswerResponse.builder()
                .correct(correct)
                .correctAnswer(exercise.getCorrectAnswer())
                .build();
    }

    private String serializeOptions(List<String> options) {
        return objectMapper.writeValueAsString(options);
    }

    private ExerciseAdminResponse toAdminResponse(Exercise exercise) {
        return ExerciseAdminResponse.builder()
                .id(exercise.getId())
                .lessonId(exercise.getLessonId())
                .exerciseType(exercise.getExerciseType())
                .questionText(exercise.getQuestionText())
                .correctAnswer(exercise.getCorrectAnswer())
                .options(objectMapper.readValue(exercise.getOptionsJson(), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class)))
                .build();
    }
}
