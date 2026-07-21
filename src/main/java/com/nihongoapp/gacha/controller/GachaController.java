package com.nihongoapp.gacha.controller;

import com.nihongoapp.common.dto.ApiResponse;
import com.nihongoapp.gacha.model.GachaBanner;
import com.nihongoapp.gacha.service.GachaService;
import com.nihongoapp.gacha.service.GachaService.GachaSpinResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/gacha")
public class GachaController {

    private final GachaService gachaService;

    public GachaController(GachaService gachaService) {
        this.gachaService = gachaService;
    }

    @GetMapping("/banners/{id}")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getBanner(@PathVariable Long id) {
        GachaBanner banner = gachaService.getBanner(id);
        return ResponseEntity.ok(ApiResponse.ok(java.util.Map.of(
                "id", banner.getId(),
                "name", banner.getName(),
                "costAmount", banner.getCostAmount()
        )));
    }

    @PostMapping("/spin")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> spin(
            Principal principal,
            @RequestBody SpinRequest request) {
        Long userId = Long.valueOf(principal.getName());
        GachaSpinResult result = gachaService.spin(userId, request.bannerId(), request.idempotencyKey());
        return ResponseEntity.ok(ApiResponse.ok(java.util.Map.of(
                "itemName", result.itemName(),
                "rarity", result.rarity(),
                "effectType", result.effectType(),
                "effectValue", result.effectValue()
        )));
    }

    public record SpinRequest(
            @NotNull Long bannerId,
            @NotBlank String idempotencyKey
    ) {}
}
