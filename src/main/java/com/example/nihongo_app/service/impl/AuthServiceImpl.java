package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.LoginRequest;
import com.example.nihongo_app.dto.request.RegisterRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.AuthService;
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

        String displayName = toDisplayName(request.getEmail());

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword())
                .displayName(displayName)
                .role("LEARNER")
                .build();

        User savedUser = userRepository.save(user);
        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!request.getPassword().equals(user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(user))
                .build();
    }

    private String toDisplayName(String email) {
        int atIndex = email.indexOf('@');
        String localPart = atIndex > 0 ? email.substring(0, atIndex) : email;
        return localPart.length() > 100 ? localPart.substring(0, 100) : localPart;
    }
}