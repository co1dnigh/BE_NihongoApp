package com.example.nihongo_app.controller;

import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tách riêng khỏi {@code PostController} vì path gốc khác ({@code /comments}, không nằm
 * dưới {@code /posts}) — theo yêu cầu spec (DELETE /api/v1/comments/{id}).
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Social Feed - Posts", description = "Đăng bài, like, comment")
public class CommentController {

    private final PostService postService;

    @DeleteMapping("/{id}")
    @Operation(summary = "Xoá comment (soft delete) — chỉ chủ comment hoặc ADMIN")
    public ResponseEntity<Void> deleteComment(Authentication authentication, @PathVariable Long id) {
        Long userId = resolveUserId(authentication);
        postService.deleteComment(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}
