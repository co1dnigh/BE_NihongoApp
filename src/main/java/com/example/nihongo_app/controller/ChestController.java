package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.ChestOpenResponse;
import com.example.nihongo_app.dto.response.ChestStatusResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.ChestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * API Rương thưởng hàng ngày: mở được 1 lần/ngày, sau khi hoàn thành đủ 3 Daily Quest.
 */
@RestController
@RequestMapping("/api/v1/users/me/chest")
@RequiredArgsConstructor
@Tag(name = "Chest", description = "Rương thưởng hàng ngày (coin ngẫu nhiên, gate bằng Daily Quest)")
public class ChestController {

    private final ChestService chestService;

    @GetMapping
    @Operation(summary = "Xem trạng thái rương thưởng hôm nay (đủ điều kiện mở chưa, đã mở chưa)")
    public ResponseEntity<ChestStatusResponse> getStatus(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(chestService.getStatus(userId));
    }

    @PostMapping("/open")
    @Operation(summary = "Mở rương thưởng hôm nay, nhận coin ngẫu nhiên (30-100). Yêu cầu đã hoàn thành đủ 3 Daily Quest và chưa mở hôm nay")
    public ResponseEntity<ChestOpenResponse> openChest(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(chestService.openChest(userId));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}
