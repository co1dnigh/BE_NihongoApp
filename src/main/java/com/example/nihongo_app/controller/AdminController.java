package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.CreateAdminRequest;
import com.example.nihongo_app.dto.response.AdminSummaryResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/users/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<AdminSummaryResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        });

        String username = generateAdminUsername(request.getEmail());

        User admin = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName().trim())
                .username(username)
                .role("ADMIN")
                .build();

        User saved = userRepository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                AdminSummaryResponse.builder()
                        .id(saved.getId())
                        .email(saved.getEmail())
                        .username(saved.getUsername())
                        .displayName(saved.getDisplayName())
                        .role(saved.getRole())
                        .build()
        );
    }

    private String generateAdminUsername(String email) {
        String localPart = email.substring(0, Math.max(email.indexOf('@'), 0));
        String normalized = localPart.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (normalized.isBlank()) {
            normalized = "admin";
        }

        String candidate = "admin_" + normalized;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = "admin_" + normalized + suffix;
            suffix++;
        }
        return candidate;
    }
}
