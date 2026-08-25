package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.VocabularyReviewRequest;
import com.example.nihongo_app.dto.response.VocabularyDueResponse;
import com.example.nihongo_app.dto.response.VocabularyReviewResponse;
import com.example.nihongo_app.dto.response.VocabularyItemResponse;
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

import java.util.List;

/**
 * Kho từ vựng + ôn tập ngắt quãng.
 *
 * <p>Khác {@link MistakeReviewController} ở đơn vị theo dõi: ngân hàng lỗi sai bám vào
 * CÂU HỎI, còn ở đây bám vào TỪ. Ôn theo câu hỏi thì người học nhớ đáp án của câu đó
 * chứ chưa chắc nhớ từ; ôn theo từ thì mọi lần gặp từ ở bất kỳ bài nào đều cộng dồn.</p>
 */
@RestController
@RequestMapping("/api/v1/vocabulary")
@RequiredArgsConstructor
@Tag(name = "Vocabulary & SRS", description = "Kho từ vựng dùng chung và lịch ôn tập ngắt quãng")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping("/due")
    @Operation(summary = "Các từ đã tới hạn ôn hôm nay, quá hạn lâu nhất trước")
    public ResponseEntity<VocabularyDueResponse> getDue(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(vocabularyService.getDue(resolveUserId(authentication), limit));
    }

    @PostMapping("/review/submit")
    @Operation(summary = "Nộp kết quả phiên ôn từ vựng, cập nhật lịch ôn theo SM-2")
    public ResponseEntity<VocabularyReviewResponse> submitReview(
            Authentication authentication,
            @RequestBody VocabularyReviewRequest request) {
        return ResponseEntity.ok(vocabularyService.submitReview(resolveUserId(authentication), request));
    }

    @GetMapping("/learned")
    @Operation(summary = "Sổ tay từ đã học, mới nhất trước")
    public ResponseEntity<VocabularyDueResponse> getLearned(
            Authentication authentication,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(vocabularyService.getLearned(resolveUserId(authentication), limit));
    }

    @GetMapping("/glossary")
    @Operation(summary = "Toàn bộ kho từ để client cache làm từ điển tra tại chỗ")
    public ResponseEntity<List<VocabularyItemResponse>> getGlossary() {
        return ResponseEntity.ok(vocabularyService.getGlossary());
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}
