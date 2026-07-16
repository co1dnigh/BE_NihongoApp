package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.LoginRequest;
import com.example.nihongo_app.dto.request.RegisterRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.AuthService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        });

        String displayName = request.getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            displayName = toDisplayName(request.getEmail());
        } else {
            displayName = displayName.trim();
        }

        String username = generateUsername(request.getEmail());

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword())
                .displayName(displayName)
                .username(username)
                .role("LEARNER")
                .build();

        User savedUser = userRepository.save(user);
        return toAuthResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!request.getPassword().equals(user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return toAuthResponse(user);
    }

    private String toDisplayName(String email) {
        int atIndex = email.indexOf('@');
        String localPart = atIndex > 0 ? email.substring(0, atIndex) : email;
        return localPart.length() > 100 ? localPart.substring(0, 100) : localPart;
    }

    private String generateUsername(String email) {
        String localPart = email == null ? "user" : email.substring(0, Math.max(email.indexOf('@'), 0));
        String normalized = localPart.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (normalized.isBlank()) {
            normalized = "user";
        }

        String candidate = normalized;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = normalized + suffix;
            suffix++;
        }
        return candidate;
    }

    private AuthResponse toAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(user))
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .build())
                .build();
    }
}