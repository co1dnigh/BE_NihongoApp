package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.LessonDetailResponse;
import com.example.nihongo_app.dto.response.LessonSummaryResponse;
import com.example.nihongo_app.service.LessonService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    public ResponseEntity<List<LessonSummaryResponse>> getLessonsByLevel(Principal principal,
                                                                         @RequestParam String level) {
        return ResponseEntity.ok(lessonService.getLessonsByLevel(principal.getName(), level));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDetailResponse> getLessonDetail(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLessonDetail(principal.getName(), id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeLesson(Principal principal, @PathVariable Long id) {
        lessonService.completeLesson(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
