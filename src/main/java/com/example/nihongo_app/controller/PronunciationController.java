package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.VocabularyReviewRequest;
import com.example.nihongo_app.dto.response.VocabularyDueResponse;
import com.example.nihongo_app.dto.response.VocabularyReviewResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.VocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/pronunciation")
@RequiredArgsConstructor
@Tag(name = "Pronunciation SRS")
public class PronunciationController {

    private final VocabularyService vocabularyService;

    @GetMapping("/due")
    @Operation(summary = "Các câu phát âm tới hạn")
    public ResponseEntity<VocabularyDueResponse> getDue(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = resolveUserId(authentication);
        
        VocabularyDueResponse res = vocabularyService.getPronunciationDue(userId, limit);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/review/submit")
    @Operation(summary = "Nộp kết quả luyện phát âm")
    public ResponseEntity<VocabularyReviewResponse> submitReview(
            Authentication authentication,
            @RequestBody VocabularyReviewRequest request) {
        // Dùng chung submitReview của Vocabulary vì thuật toán SM-2 giống hệt!
        return ResponseEntity.ok(vocabularyService.submitReview(resolveUserId(authentication), request));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}
