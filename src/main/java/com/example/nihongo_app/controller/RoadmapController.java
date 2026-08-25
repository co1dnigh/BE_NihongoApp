package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.RoadmapTopicResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * API lấy lộ trình học (bản đồ) cho user hiện tại.
 *
 * <p>Mỗi bài học được gắn 1 trong 3 trạng thái:
 * {@code LOCKED}, {@code UNLOCKED}, {@code COMPLETED}; với bài TOPIC_REVIEW
 * còn trả kèm số sao đã đạt được.</p>
 */
@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
@Tag(name = "Roadmap", description = "Lộ trình học (Topic + Lesson kèm trạng thái khoá/mở)")
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping
    @Operation(summary = "Lấy lộ trình học của user hiện tại (danh sách Topic, mỗi Topic kèm Lesson + trạng thái LOCKED/UNLOCKED/COMPLETED)")
    public ResponseEntity<List<RoadmapTopicResponse>> getRoadmap(Authentication authentication) {
        // AppUserPrincipal được JwtAuthenticationFilter set vào SecurityContextHolder,
        // giúp lấy userId trực tiếp mà không cần query thêm UserRepository.
        AppUserPrincipal principal = resolvePrincipal(authentication);
        return ResponseEntity.ok(roadmapService.getRoadmap(principal.getUserId()));
    }

    private AppUserPrincipal resolvePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            // Trường hợp này về lý thuyết không xảy ra vì SecurityConfig.anyRequest().authenticated()
            // đã chặn request thiếu JWT ngay từ filter. Phòng trường hợp cấu hình bị sai.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal;
    }
}