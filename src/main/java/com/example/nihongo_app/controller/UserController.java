package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.SyncContactsRequest;
import com.example.nihongo_app.dto.request.UpdateAvatarRequest;
import com.example.nihongo_app.dto.request.UpdatePhoneRequest;
import com.example.nihongo_app.dto.request.UpdateProfileRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.dto.response.AvatarUrlResponse;
import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.dto.response.FollowSummaryResponse;
import com.example.nihongo_app.dto.response.UserOverviewResponse;
import com.example.nihongo_app.dto.response.UserProfileResponse;
import com.example.nihongo_app.dto.response.UserPublicProfileResponse;
import com.example.nihongo_app.dto.response.UserSearchResponse;
import com.example.nihongo_app.dto.response.UserStatsResponse;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Hồ sơ cá nhân, bạn bè, tìm kiếm")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Lấy toàn bộ trạng thái gamification của user hiện tại (level/exp/league/coins/energy/streak) trong 1 lần gọi — dùng để FE hydrate UI ngay sau khi có token")
    public ResponseEntity<UserStatsResponse> getMyStats(Principal principal) {
        return ResponseEntity.ok(userService.getMyStats(principal.getName()));
    }

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
    @Operation(summary = "Tìm kiếm user theo từ khoá (username/display name/email), có phân trang cursor. "
            + "⚠️ Đổi shape response so với trước: trả CursorPageResponse thay vì mảng trần")
    public ResponseEntity<CursorPageResponse<UserSearchResponse>> searchUsers(
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = resolveUserId(authentication);
        // Giu "keyword" de tuong thich nguoc (endpoint nay truoc day chi nhan keyword),
        // "q" la ten param moi theo dung spec.
        String searchTerm = q != null ? q : keyword;
        return ResponseEntity.ok(userService.searchUsersCursor(userId, searchTerm, cursor, size));
    }

    // ============================ Follow (module Social Feed) ============================

    @PutMapping("/{id}/follow")
    @Operation(summary = "Follow 1 user — idempotent (đã follow rồi gọi lại không lỗi, không đổi state)")
    public ResponseEntity<Void> followUser(Authentication authentication, @PathVariable Long id) {
        Long userId = resolveUserId(authentication);
        userService.followUser(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/follow")
    @Operation(summary = "Unfollow 1 user — idempotent (chưa follow gọi vẫn không lỗi)")
    public ResponseEntity<Void> unfollowUser(Authentication authentication, @PathVariable Long id) {
        Long userId = resolveUserId(authentication);
        userService.unfollowUser(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/followers")
    @Operation(summary = "Danh sách người đang follow user này (phân trang cursor)")
    public ResponseEntity<CursorPageResponse<FollowSummaryResponse>> getFollowers(
            Authentication authentication, @PathVariable Long id,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = resolveUserId(authentication);
        return ResponseEntity.ok(userService.getFollowers(id, currentUserId, cursor, size));
    }

    @GetMapping("/{id}/following")
    @Operation(summary = "Danh sách người mà user này đang follow (phân trang cursor)")
    public ResponseEntity<CursorPageResponse<FollowSummaryResponse>> getFollowing(
            Authentication authentication, @PathVariable Long id,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = resolveUserId(authentication);
        return ResponseEntity.ok(userService.getFollowing(id, currentUserId, cursor, size));
    }

    @GetMapping("/{id}/profile")
    @Operation(summary = "Hồ sơ công khai của 1 user theo id (rank, streak, follower/following count) — "
            + "khác GET /profile/{username} tra theo username")
    public ResponseEntity<UserPublicProfileResponse> getPublicProfile(
            Authentication authentication, @PathVariable Long id) {
        Long currentUserId = resolveUserId(authentication);
        return ResponseEntity.ok(userService.getPublicProfile(currentUserId, id));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
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

    @GetMapping("/me/avatar")
    @Operation(summary = "Lấy avatar URL của user hiện tại")
    public ResponseEntity<AvatarUrlResponse> getAvatarUrl(Principal principal) {
        return ResponseEntity.ok(AvatarUrlResponse.builder()
                .avatarUrl(userService.getAvatarUrl(principal.getName()))
                .build());
    }

    @PutMapping("/me/avatar")
    @Operation(summary = "Cập nhật avatar URL của user hiện tại")
    public ResponseEntity<Void> updateAvatarUrl(Principal principal,
                                                 @Valid @RequestBody UpdateAvatarRequest request) {
        userService.updateAvatarUrl(principal.getName(), request.getAvatarUrl());
        return ResponseEntity.noContent().build();
    }
}