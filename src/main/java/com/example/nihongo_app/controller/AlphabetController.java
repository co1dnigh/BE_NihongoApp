package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.AlphabetMatrixGroupResponse;
import com.example.nihongo_app.dto.response.AlphabetPracticeResponse;
import com.example.nihongo_app.dto.response.SubmitAlphabetPracticeResponse;
import com.example.nihongo_app.entity.Character.CharacterType;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.AlphabetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/alphabets")
@RequiredArgsConstructor
@Tag(name = "Alphabet Practice", description = "Luyện tập bảng chữ cái")
public class AlphabetController {

    private final AlphabetService alphabetService;

    @GetMapping
    @Operation(summary = "Lấy ma trận chữ cái kèm mức thông thạo")
    public ResponseEntity<List<AlphabetMatrixGroupResponse>> getMatrix(
            @RequestParam CharacterType type, Authentication authentication) {
        return ResponseEntity.ok(alphabetService.getMatrix(type, resolveUserId(authentication)));
    }

    @PostMapping("/practice/start")
    @Operation(summary = "Sinh đề luyện tập bảng chữ cái")
    public ResponseEntity<AlphabetPracticeResponse> startPractice(Authentication authentication) {
        return ResponseEntity.ok(alphabetService.startPractice(resolveUserId(authentication)));
    }

    @PostMapping("/practice/submit")
    @Operation(summary = "Nộp kết quả luyện tập và nhận EXP")
    public ResponseEntity<SubmitAlphabetPracticeResponse> submitPractice(
            @Valid @RequestBody com.example.nihongo_app.dto.request.SubmitAlphabetPracticeRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(alphabetService.submitPractice(resolveUserId(authentication), request));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}