package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.ExerciseRequest;
import com.example.nihongo_app.dto.request.SubmitAnswerRequest;
import com.example.nihongo_app.dto.response.ExerciseAdminResponse;
import com.example.nihongo_app.dto.response.SubmitAnswerResponse;

public interface ExerciseService {

    ExerciseAdminResponse createExercise(ExerciseRequest request);

    ExerciseAdminResponse updateExercise(Long id, ExerciseRequest request);

    void deleteExercise(Long id);

    SubmitAnswerResponse submitAnswer(String email, Long exerciseId, SubmitAnswerRequest request);
}
