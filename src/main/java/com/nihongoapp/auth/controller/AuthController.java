package com.nihongoapp.auth.controller;

import com.nihongoapp.auth.service.TokenService;
import com.nihongoapp.auth.service.UserAuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserAuthService userAuthService;
    private final TokenService tokenService;

    public AuthController(UserAuthService userAuthService, TokenService tokenService) {
        this.userAuthService = userAuthService;
        this.tokenService = tokenService;
    }

    public record LoginReq(@NotBlank String username, @NotBlank @Size(min = 4) String password) {}
    public record RefreshReq(@NotBlank String refreshToken) {}
    public record TokenRes(String accessToken, String refreshToken) {}

    @PostMapping("/login")
    public ResponseEntity<TokenRes> login(@Valid @RequestBody LoginReq req) {
        var user = userAuthService.authenticate(req.username(), req.password());
        var pair = tokenService.generateTokens(user);
        return ResponseEntity.ok(new TokenRes(pair.accessToken(), pair.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRes> refresh(@Valid @RequestBody RefreshReq req) {
        return tokenService.rotateRefreshToken(req.refreshToken())
                .map(pair -> ResponseEntity.ok(new TokenRes(pair.accessToken(), pair.refreshToken())))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshReq req) {
        tokenService.revokeRefreshToken(req.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
