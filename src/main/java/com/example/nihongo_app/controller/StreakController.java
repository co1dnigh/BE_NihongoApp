package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.StreakResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.StreakService;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/users/me/streak")
@RequiredArgsConstructor
public class StreakController {

private final StreakService streakService;
private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

@GetMapping
public ResponseEntity<StreakResponse> getStreak(Authentication authentication) {
Long userId = resolveUserId(authentication);
User user = streakService.getUserForStreak(userId);
StreakResponse response = StreakResponse.builder()
.currentStreak(Objects.requireNonNullElse(user.getCurrentStreak(), 0))
.longestStreak(Objects.requireNonNullElse(user.getLongestStreak(), 0))
.lastStreakDate(user.getLastStreakDate() != null ? user.getLastStreakDate().format(DATE_FORMATTER) : null)
.streakFreezeCount(Objects.requireNonNullElse(user.getStreakFreezeCount(), 0))
.build();
return ResponseEntity.ok(response);
}

private Long resolveUserId(Authentication authentication) {
if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
}
return principal.getUserId();
}
}
