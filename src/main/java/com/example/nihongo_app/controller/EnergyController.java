package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.EnergyResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.EnergyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/users/me/energy")
@RequiredArgsConstructor
@Tag(name = "Energy", description = "Năng lượng để bắt đầu bài học")
public class EnergyController {

private final EnergyService energyService;

@GetMapping
@Operation(summary = "Xem năng lượng hiện tại (tự động hồi theo số ngày trôi qua trước khi trả về)")
public ResponseEntity<EnergyResponse> getEnergy(Authentication authentication) {
Long userId = resolveUserId(authentication);
energyService.recoverEnergy(userId);
var user = energyService.getUserForRead(userId);
EnergyResponse response = EnergyResponse.builder()
.currentEnergy(Objects.requireNonNullElse(user.getCurrentEnergy(), 0))
.maxEnergy(Objects.requireNonNullElse(user.getMaxEnergy(), 0))
.lastRecoveryDate(user.getLastEnergyResetDate() != null ? user.getLastEnergyResetDate().toString() : null)
.build();
return ResponseEntity.ok(response);
}

@PostMapping("/practice")
@Operation(summary = "Cộng thêm 1 năng lượng (thưởng luyện tập)")
public ResponseEntity<EnergyResponse> practice(Authentication authentication) {
Long userId = resolveUserId(authentication);
energyService.addEnergy(userId, 1);
var user = energyService.getUserForRead(userId);
EnergyResponse response = EnergyResponse.builder()
.currentEnergy(Objects.requireNonNullElse(user.getCurrentEnergy(), 0))
.maxEnergy(Objects.requireNonNullElse(user.getMaxEnergy(), 0))
.lastRecoveryDate(user.getLastEnergyResetDate() != null ? user.getLastEnergyResetDate().toString() : null)
.build();
return ResponseEntity.ok(response);
}

@PostMapping("/ads")
@Operation(summary = "Xem quảng cáo để hồi thêm 5 năng lượng (cooldown 30 phút/lần)")
public ResponseEntity<EnergyResponse> watchAd(Authentication authentication) {
Long userId = resolveUserId(authentication);
energyService.watchAd(userId);
var user = energyService.getUserForRead(userId);
EnergyResponse response = EnergyResponse.builder()
.currentEnergy(Objects.requireNonNullElse(user.getCurrentEnergy(), 0))
.maxEnergy(Objects.requireNonNullElse(user.getMaxEnergy(), 0))
.lastRecoveryDate(user.getLastEnergyResetDate() != null ? user.getLastEnergyResetDate().toString() : null)
.build();
return ResponseEntity.ok(response);
}

@PostMapping("/refill")
@Operation(summary = "Hồi đầy năng lượng ngay bằng cách trừ coin (400 coin/lần)")
public ResponseEntity<EnergyResponse> refill(Authentication authentication) {
Long userId = resolveUserId(authentication);
energyService.refillWithCoins(userId);
var user = energyService.getUserForRead(userId);
EnergyResponse response = EnergyResponse.builder()
.currentEnergy(Objects.requireNonNullElse(user.getCurrentEnergy(), 0))
.maxEnergy(Objects.requireNonNullElse(user.getMaxEnergy(), 0))
.lastRecoveryDate(user.getLastEnergyResetDate() != null ? user.getLastEnergyResetDate().toString() : null)
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
