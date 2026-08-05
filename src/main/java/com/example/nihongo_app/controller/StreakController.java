package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.StreakResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/users/me/streak")
@RequiredArgsConstructor
@Tag(name = "Streak", description = "Chuỗi ngày học liên tiếp + Streak Freeze")
public class StreakController {

private final StreakService streakService;
private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

@GetMapping
@Operation(summary = "Xem streak hiện tại (số ngày liên tiếp, streak dài nhất, số lượt Freeze còn lại)")
public ResponseEntity<StreakResponse> getStreak(Authentication authentication) {
Long userId = resolveUserId(authentication);
User user = streakService.getUserForStreak(userId);
return ResponseEntity.ok(toResponse(user));
}

@PostMapping("/freeze/buy")
@Operation(summary = "Mua thêm 1 lượt Streak Freeze bằng coin (200 coin/lượt)")
public ResponseEntity<StreakResponse> buyStreakFreeze(Authentication authentication) {
Long userId = resolveUserId(authentication);
User user = streakService.buyStreakFreeze(userId);
return ResponseEntity.ok(toResponse(user));
}

private StreakResponse toResponse(User user) {
return StreakResponse.builder()
.currentStreak(Objects.requireNonNullElse(user.getCurrentStreak(), 0))
.longestStreak(Objects.requireNonNullElse(user.getLongestStreak(), 0))
.lastStreakDate(user.getLastStreakDate() != null ? user.getLastStreakDate().format(DATE_FORMATTER) : null)
.streakFreezeCount(Objects.requireNonNullElse(user.getStreakFreezeCount(), 0))
.build();
}

private Long resolveUserId(Authentication authentication) {
if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
}
return principal.getUserId();
}
}
