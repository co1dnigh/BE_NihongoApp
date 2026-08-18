package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.ReviewSubmitRequest;
import com.example.nihongo_app.dto.response.MistakeSummaryResponse;
import com.example.nihongo_app.dto.response.ReviewSessionResponse;
import com.example.nihongo_app.dto.response.ReviewSubmitResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.MistakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * API "Mistake Bank": ôn lại riêng các câu hỏi user hay sai (khác luồng học bài thường ở
 * {@link LessonAttemptController}) — không trừ năng lượng lúc bắt đầu, thưởng năng lượng
 * (giới hạn lượt/ngày) khi hoàn thành, không cộng EXP/Coin để tránh farm.
 */
@RestController
@RequestMapping("/api/v1/reviews/mistakes")
@RequiredArgsConstructor
@Tag(name = "Mistake Review", description = "Ngân hàng lỗi sai và phiên ôn tập riêng")
public class MistakeReviewController {

    private final MistakeService mistakeService;

    @GetMapping("/summary")
    @Operation(summary = "Xem số câu đang cần ôn (ACTIVE) và số phiên còn được thưởng năng lượng hôm nay")
    public ResponseEntity<MistakeSummaryResponse> getSummary(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(mistakeService.getSummary(userId));
    }

    @PostMapping("/start")
    @Operation(summary = "Bắt đầu phiên ôn lỗi sai: lấy các câu sai nhiều/gần nhất, không trừ năng lượng, không lộ đáp án đúng")
    public ResponseEntity<ReviewSessionResponse> startReviewSession(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(mistakeService.startReviewSession(userId));
    }

    @PostMapping("/submit")
    @Operation(summary = "Nộp kết quả phiên ôn: server tự chấm từng câu, cập nhật Mistake Bank, thưởng năng lượng nếu còn lượt trong ngày")
    public ResponseEntity<ReviewSubmitResponse> submitReviewSession(Authentication authentication,
                                                                      @RequestBody ReviewSubmitRequest request) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(mistakeService.submitReviewSession(userId, request));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}
