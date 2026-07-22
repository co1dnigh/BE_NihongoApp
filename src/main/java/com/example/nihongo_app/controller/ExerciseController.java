package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.SubmitAnswerRequest;
import com.example.nihongo_app.dto.response.SubmitAnswerResponse;
import com.example.nihongo_app.service.ExerciseService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(Principal principal, @PathVariable Long id,
                                                             @Valid @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(exerciseService.submitAnswer(principal.getName(), id, request));
    }
}
