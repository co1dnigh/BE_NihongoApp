package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.ConsumeItemResponse;
import com.example.nihongo_app.dto.response.InventoryItemResponse;
import com.example.nihongo_app.dto.response.ShopItemResponse;
import com.example.nihongo_app.dto.response.ShopPurchaseResponse;
import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/users/me/shop")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "Cửa hàng vật phẩm (mua bằng coin)")
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    @Operation(summary = "Xem danh sách vật phẩm trong shop (nhóm theo loại: Consumable, Powerup, Cosmetic)")
    public ResponseEntity<Map<ShopItem.ItemType, List<ShopItemResponse>>> getShopItems() {
        return ResponseEntity.ok(shopService.getShopItems());
    }

    @PostMapping("/buy/{itemId}")
    @Operation(summary = "Mua vật phẩm từ shop")
    public ResponseEntity<ShopPurchaseResponse> buyItem(Authentication authentication, @PathVariable Long itemId) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(shopService.purchaseItem(userId, itemId));
    }

    @GetMapping("/inventory")
    @Operation(summary = "Xem túi đồ của user")
    public ResponseEntity<List<InventoryItemResponse>> getInventory(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(shopService.getUserInventory(userId));
    }

    @PostMapping("/inventory/{inventoryId}/consume")
    @Operation(summary = "Sử dụng vật phẩm tiêu hao (streak freeze, energy refill) hoặc kích hoạt powerup (double xp, double coin, timer boost)")
    public ResponseEntity<ConsumeItemResponse> consumeItem(Authentication authentication, @PathVariable Long inventoryId) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(shopService.consumeItem(userId, inventoryId));
    }

    @PostMapping("/inventory/{inventoryId}/equip")
    @Operation(summary = "Trang bị/bỏ trang bị vật phẩm trang trí (avatar frame, badge, theme)")
    public ResponseEntity<InventoryItemResponse> equipCosmetic(Authentication authentication, @PathVariable Long inventoryId) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(shopService.equipCosmetic(userId, inventoryId));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}