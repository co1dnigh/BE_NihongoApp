package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.CreateCommentRequest;
import com.example.nihongo_app.dto.request.CreatePostRequest;
import com.example.nihongo_app.dto.response.CommentResponse;
import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Đăng bài / like / comment trong bản tin cộng đồng. Xem thêm {@code FeedController}
 * (đọc feed tổng hợp) và {@code CommentController} (xoá 1 comment theo id riêng).
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Social Feed - Posts", description = "Đăng bài, like, comment")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Đăng trạng thái mới (postType = USER_STATUS)")
    public ResponseEntity<Void> createPost(Authentication authentication,
                                           @Valid @RequestBody CreatePostRequest request) {
        Long userId = resolveUserId(authentication);
        postService.createUserStatusPost(userId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xoá bài đăng (soft delete) — chỉ chủ bài hoặc ADMIN")
    public ResponseEntity<Void> deletePost(Authentication authentication, @PathVariable Long id) {
        Long userId = resolveUserId(authentication);
        postService.deletePost(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like bài đăng — idempotent (like lại không lỗi)")
    public ResponseEntity<Void> likePost(Authentication authentication, @PathVariable Long id) {
        Long userId = resolveUserId(authentication);
        postService.likePost(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Bỏ like — idempotent (chưa like gọi vẫn không lỗi)")
    public ResponseEntity<Void> unlikePost(Authentication authentication, @PathVariable Long id) {
        Long userId = resolveUserId(authentication);
        postService.unlikePost(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Danh sách comment của 1 bài đăng (phân trang cursor)")
    public ResponseEntity<CursorPageResponse<CommentResponse>> listComments(
            @PathVariable Long id,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.listComments(id, cursor, size));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Thêm comment vào 1 bài đăng")
    public ResponseEntity<Void> addComment(Authentication authentication, @PathVariable Long id,
                                           @Valid @RequestBody CreateCommentRequest request) {
        Long userId = resolveUserId(authentication);
        postService.addComment(userId, id, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}
