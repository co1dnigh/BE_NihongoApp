package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.SyncContactsRequest;
import com.example.nihongo_app.dto.request.UpdatePhoneRequest;
import com.example.nihongo_app.dto.request.UpdateProfileRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.dto.response.UserOverviewResponse;
import com.example.nihongo_app.dto.response.UserProfileResponse;
import com.example.nihongo_app.dto.response.UserSearchResponse;
import com.example.nihongo_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Hồ sơ cá nhân, bạn bè, tìm kiếm")
public class UserController {

    private final UserService userService;

    @PutMapping("/me/phone")
    @Operation(summary = "Cập nhật số điện thoại của user hiện tại")
    public ResponseEntity<Void> updatePhoneNumber(Principal principal,
                                                  @Valid @RequestBody UpdatePhoneRequest request) {
        userService.updatePhoneNumber(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync-contacts")
    @Operation(summary = "Đồng bộ danh bạ điện thoại để tìm bạn bè đã dùng app")
    public ResponseEntity<List<UserOverviewResponse>> syncContacts(@Valid @RequestBody SyncContactsRequest request) {
        return ResponseEntity.ok(userService.findFriendsFromContacts(request));
    }

    @PostMapping("/{id}/follow")
    @Operation(summary = "Theo dõi / bỏ theo dõi 1 user (toggle theo trạng thái hiện tại)")
    public ResponseEntity<Boolean> toggleFollow(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleFollow(principal.getName(), id));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm user theo từ khoá (username/display name)")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(Principal principal,
                                                                @RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUsers(principal.getName(), keyword));
    }

    @GetMapping("/profile/{username}")
    @Operation(summary = "Xem hồ sơ công khai của 1 user theo username")
    public ResponseEntity<UserProfileResponse> getProfileByUsername(Principal principal,
                                                                    @PathVariable String username) {
        return ResponseEntity.ok(userService.getProfileByUsername(principal.getName(), username));
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Cập nhật hồ sơ cá nhân (display name, username...)")
    public ResponseEntity<AuthResponse> updateProfile(Principal principal,
                                                      @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), request));
    }
}