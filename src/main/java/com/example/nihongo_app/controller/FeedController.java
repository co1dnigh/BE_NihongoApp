package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.dto.response.PostResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "Social Feed - Feed", description = "Bản tin tổng hợp (chính mình + đang follow)")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    @Operation(summary = "Lấy bản tin tổng hợp, mới nhất trước, phân trang cursor")
    public ResponseEntity<CursorPageResponse<PostResponse>> getFeed(
            Authentication authentication,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(feedService.getFeed(userId, cursor, size));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}
