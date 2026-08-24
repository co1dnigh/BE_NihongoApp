package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.LoginRequest;
import com.example.nihongo_app.dto.request.FacebookLoginRequest;
import com.example.nihongo_app.dto.request.GoogleLoginRequest;
import com.example.nihongo_app.dto.request.RegisterRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Đăng ký / đăng nhập, cấp JWT access token")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản Learner mới")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng email/password, trả về JWT access token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/social/google")
    @Operation(summary = "Đăng nhập bằng Google ID token")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @PostMapping("/social/facebook")
    @Operation(summary = "Đăng nhập bằng Facebook access token")
    public ResponseEntity<AuthResponse> loginWithFacebook(@Valid @RequestBody FacebookLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithFacebook(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất khỏi ứng dụng")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}