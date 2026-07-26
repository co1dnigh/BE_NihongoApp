package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.SubmitLessonRequest;
import com.example.nihongo_app.dto.response.CancelLessonResponse;
import com.example.nihongo_app.dto.response.StartLessonResponse;
import com.example.nihongo_app.dto.response.SubmitLessonResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.LessonAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller cho 3 API vòng đời của một lượt làm bài:
 * <ul>
 *   <li>{@code POST /api/v1/lessons/{id}/start}   — bắt đầu bài (trừ năng lượng + phát đề).</li>
 *   <li>{@code POST /api/v1/lessons/{id}/submit}  — nộp bài (chấm điểm + trả thưởng).</li>
 *   <li>{@code POST /api/v1/lessons/{id}/cancel}  — huỷ bài (hoàn năng lượng).</li>
 * </ul>
 *
 * <p>Không cần kiểm tra role vì SecurityConfig đã mặc định {@code anyRequest().authenticated()}
 * và các API này chỉ cần user đăng nhập (mọi learner đều có quyền).</p>
 */
@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonAttemptController {

    private final LessonAttemptService lessonAttemptService;

    @PostMapping("/{id}/start")
    public ResponseEntity<StartLessonResponse> startLesson(@PathVariable("id") Long lessonId,
                                                          Authentication authentication) {
        Long userId = requireUserId(authentication);
        return ResponseEntity.ok(lessonAttemptService.startLesson(lessonId, userId));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmitLessonResponse> submitLesson(@PathVariable("id") Long lessonId,
                                                             @Valid @RequestBody SubmitLessonRequest request,
                                                             Authentication authentication) {
        Long userId = requireUserId(authentication);
        return ResponseEntity.status(HttpStatus.OK)
                .body(lessonAttemptService.submitLesson(lessonId, userId, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CancelLessonResponse> cancelLesson(@PathVariable("id") Long lessonId,
                                                             Authentication authentication) {
        Long userId = requireUserId(authentication);
        return ResponseEntity.ok(lessonAttemptService.cancelLesson(lessonId, userId));
    }

    private Long requireUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}