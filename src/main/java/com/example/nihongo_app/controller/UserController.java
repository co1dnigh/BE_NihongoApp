package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.SyncContactsRequest;
import com.example.nihongo_app.dto.request.UpdatePhoneRequest;
import com.example.nihongo_app.dto.request.UpdateProfileRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.dto.response.UserOverviewResponse;
import com.example.nihongo_app.dto.response.UserProfileResponse;
import com.example.nihongo_app.dto.response.UserSearchResponse;
import com.example.nihongo_app.service.UserService;
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
public class UserController {

    private final UserService userService;

    @PutMapping("/me/phone")
    public ResponseEntity<Void> updatePhoneNumber(Principal principal,
                                                  @Valid @RequestBody UpdatePhoneRequest request) {
        userService.updatePhoneNumber(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync-contacts")
    public ResponseEntity<List<UserOverviewResponse>> syncContacts(@Valid @RequestBody SyncContactsRequest request) {
        return ResponseEntity.ok(userService.findFriendsFromContacts(request));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Boolean> toggleFollow(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleFollow(principal.getName(), id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(Principal principal,
                                                                @RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUsers(principal.getName(), keyword));
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileResponse> getProfileByUsername(Principal principal,
                                                                    @PathVariable String username) {
        return ResponseEntity.ok(userService.getProfileByUsername(principal.getName(), username));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<AuthResponse> updateProfile(Principal principal,
                                                      @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), request));
    }
}