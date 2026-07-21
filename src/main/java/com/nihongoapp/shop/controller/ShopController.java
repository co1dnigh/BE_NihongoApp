package com.nihongoapp.shop.controller;

import com.nihongoapp.common.dto.ApiResponse;
import com.nihongoapp.shop.service.ShopService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/shop")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<ShopService.ShopPurchaseResult>> purchase(
            Principal principal,
            @Valid @RequestBody PurchaseRequest request) {
        Long userId = Long.parseLong(principal.getName());
        ShopService.ShopPurchaseResult result = shopService.purchase(userId, request.shopItemId(), request.idempotencyKey());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    public record PurchaseRequest(
            @NotNull Long shopItemId,
            @NotNull String idempotencyKey
    ) {}
}
