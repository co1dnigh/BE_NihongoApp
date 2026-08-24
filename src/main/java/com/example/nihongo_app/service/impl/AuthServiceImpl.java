package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.LoginRequest;
import com.example.nihongo_app.dto.request.RegisterRequest;
import com.example.nihongo_app.dto.request.FacebookLoginRequest;
import com.example.nihongo_app.dto.request.GoogleLoginRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.RankRepository;
import com.example.nihongo_app.entity.User.AuthProvider;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.AuthService;
import java.util.Locale;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

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
        Rank defaultRank = rankRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Missing default rank row with id=1"));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(displayName)
                .username(username)
                .role("LEARNER")
                .rank(defaultRank)
                .build();

        User savedUser = userRepository.save(user);
        return toAuthResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return toAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload;
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(request.getIdToken());
            if (idToken == null || !Boolean.TRUE.equals(idToken.getPayload().getEmailVerified())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or unverified Google ID token");
            }
            payload = idToken.getPayload();
        } catch (IOException | GeneralSecurityException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid Google ID token", ex);
        }

        return loginOrCreateSocialUser(
                payload.getEmail(),
                payload.getSubject(),
                AuthProvider.GOOGLE,
                stringClaim(payload, "name"),
                stringClaim(payload, "picture"));
    }

    @Override
    @Transactional
    public AuthResponse loginWithFacebook(FacebookLoginRequest request) {
        try {
            var uri = UriComponentsBuilder
                    .fromUriString("https://graph.facebook.com/me")
                    .queryParam("fields", "id,email,name")
                    .queryParam("access_token", request.getAccessToken())
                    .build()
                    .toUri();
            FacebookProfile profile = restTemplate.getForObject(uri, FacebookProfile.class);

            if (profile == null || profile.id() == null || profile.id().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid Facebook access token");
            }
            if (profile.email() == null || profile.email().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Facebook email permission is required");
            }

            return loginOrCreateSocialUser(
                    profile.email(), profile.id(), AuthProvider.FACEBOOK,
                    profile.name(), null);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid Facebook access token", ex);
        }
    }

    private AuthResponse loginOrCreateSocialUser(String email, String providerId,
                                                  AuthProvider provider,
                                                  String displayName, String avatarUrl) {
        if (email == null || email.isBlank() || providerId == null || providerId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Social provider did not return a valid identity");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (user == null) {
            Rank defaultRank = rankRepository.findById(1L)
                    .orElseThrow(() -> new IllegalStateException("Missing default rank row with id=1"));
            String safeDisplayName = displayName == null || displayName.isBlank()
                    ? toDisplayName(normalizedEmail) : displayName.trim();
            user = User.builder()
                    .email(normalizedEmail)
                    .passwordHash(null)
                    .authProvider(provider)
                    .providerId(providerId)
                    .displayName(safeDisplayName)
                    .username(generateUsername(normalizedEmail))
                    .role("LEARNER")
                    .rank(defaultRank)
                    .avatarUrl(avatarUrl)
                    .build();
        } else {
            user.setAuthProvider(provider);
            user.setProviderId(providerId);
            user.setPasswordHash(null);
        }

        return toAuthResponse(userRepository.save(user));
    }

    private String stringClaim(GoogleIdToken.Payload payload, String name) {
        Object value = payload.get(name);
        return value == null ? null : value.toString();
    }

    private record FacebookProfile(String id, String email, String name) {
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
            .refreshToken(jwtTokenProvider.generateRefreshToken(user))
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