package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.EnergyResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.EnergyService;
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
public class EnergyController {

private final EnergyService energyService;

@GetMapping
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

@PostMapping("/refill")
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
