package com.nihongoapp.lesson.controller;

import com.nihongoapp.common.dto.ApiResponse;
import com.nihongoapp.lesson.service.LessonService;
import com.nihongoapp.lesson.service.LessonService.ExerciseResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<ApiResponse<LessonService.LessonCompleteResult>> complete(
            Principal principal,
            @PathVariable Long lessonId,
            @Valid @RequestBody CompleteRequest request) {
        Long userId = getUserId(principal);
        LessonService.LessonCompleteResult result = lessonService.completeLesson(
                userId, lessonId, request.exerciseResults(), request.idempotencyKey());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private Long getUserId(Principal principal) {
        return Long.parseLong(principal.getName());
    }

    public record CompleteRequest(
            @NotNull List<ExerciseResult> exerciseResults,
            @NotNull String idempotencyKey
    ) {}
}
