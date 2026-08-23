package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.AchievementResponse;
import com.example.nihongo_app.entity.Achievement;
import com.example.nihongo_app.entity.UserAchievement;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * API Thành tích (Achievements / Huy chương). Kiểu Duolingo: grid achievement,
 * mỗi achievement có thể secret (ẩn cho tới khi unlock).
 */
@RestController
@RequestMapping("/api/v1/users/me/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievements", description = "Hệ thống thành tích/huy chương (streak, lesson, topic...)")
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * Danh sách achievement đang active, kèm trạng thái của user (unlock/progress).
     * Secret achievement chưa unlock bị ẩn khỏi danh sách.
     */
    @GetMapping
    @Operation(summary = "Danh sách thành tích (kèm trạng thái unlock/progress)")
    public ResponseEntity<List<AchievementResponse>> listAchievements(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        List<Achievement> achievements = achievementService.listAchievements(userId);

        // Build map achievementId -> UserAchievement để điền unlocked/progress.
        Map<Long, UserAchievement> uaByAchievementId = new HashMap<>();
        achievementService.getUnlocked(userId).forEach(ua ->
                uaByAchievementId.put(ua.getAchievement().getId(), ua));
        achievementService.getProgress(userId).forEach(ua ->
                uaByAchievementId.putIfAbsent(ua.getAchievement().getId(), ua));

        List<AchievementResponse> response = achievements.stream()
                .map(a -> {
                    UserAchievement ua = uaByAchievementId.get(a.getId());
                    return AchievementResponse.builder()
                            .achievementId(a.getId())
                            .code(a.getCode())
                            .name(a.getName())
                            .description(a.getDescription())
                            .icon(a.getIcon())
                            .type(a.getType())
                            .threshold(a.getThreshold())
                            .secret(Boolean.TRUE.equals(a.getSecret()))
                            .unlocked(ua != null && ua.getUnlockedAt() != null)
                            .progress(ua != null ? ua.getProgress() : 0)
                            .active(Boolean.TRUE.equals(a.getActive()))
                            .build();
                })
                .toList();
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}