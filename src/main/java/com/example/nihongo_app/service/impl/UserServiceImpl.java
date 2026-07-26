package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.SyncContactsRequest;
import com.example.nihongo_app.dto.request.UpdatePhoneRequest;
import com.example.nihongo_app.dto.request.UpdateProfileRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.dto.response.UserOverviewResponse;
import com.example.nihongo_app.dto.response.UserSearchResponse;
import com.example.nihongo_app.dto.response.UserProfileResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void updatePhoneNumber(String email, UpdatePhoneRequest request) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String phoneNumber = request.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }

        List<User> existingUsers = userRepository.findByPhoneNumberIn(Collections.singletonList(phoneNumber));
        boolean takenByAnotherUser = existingUsers.stream()
                .anyMatch(user -> !Objects.equals(user.getId(), currentUser.getId()));
        if (takenByAnotherUser) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already in use");
        }

        currentUser.setPhoneNumber(phoneNumber);
        userRepository.save(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserOverviewResponse> findFriendsFromContacts(SyncContactsRequest request) {
        List<String> phoneNumbers = request.getPhoneNumbers();
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        return userRepository.findByPhoneNumberIn(phoneNumbers).stream()
                .map(this::toOverviewResponse)
                .collect(Collectors.toList());
    }

    private UserOverviewResponse toOverviewResponse(User user) {
        return UserOverviewResponse.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .avatarUrl(null)
                .level(user.getLevel())
                .build();
    }

    @Override
    @Transactional
    public boolean toggleFollow(String currentUserEmail, Long targetUserId) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        if (Objects.equals(currentUser.getId(), targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }

        boolean alreadyFollowing = userRepository.existsFollowRelation(currentUser.getId(), targetUser.getId()) > 0;
        if (alreadyFollowing) {
            userRepository.deleteFollow(currentUser.getId(), targetUser.getId());
            return false;
        }

        userRepository.insertFollow(currentUser.getId(), targetUser.getId());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(String currentUserEmail, String keyword) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String searchKeyword = keyword == null ? "" : keyword.trim();
        return userRepository.searchUsers(currentUser.getId(), searchKeyword).stream()
                .map(this::toSearchResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String currentUserEmail, String targetUsername) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return toProfileResponse(currentUser.getId(), targetUser);
    }

    @Override
    @Transactional
    public AuthResponse updateProfile(String currentUserEmail, UpdateProfileRequest request) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String requestedUsername = request.getUsername() == null ? null : request.getUsername().trim();
        if (requestedUsername == null || requestedUsername.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        boolean usernameChanged = currentUser.getUsername() == null || !currentUser.getUsername().equals(requestedUsername);
        if (usernameChanged && userRepository.existsByUsername(requestedUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
        }

        String displayName = request.getDisplayName() == null ? null : request.getDisplayName().trim();
        if (displayName == null || displayName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Display name is required");
        }

        currentUser.setDisplayName(displayName);
        currentUser.setUsername(requestedUsername);

        User savedUser = userRepository.save(currentUser);
        return toAuthResponse(savedUser);
    }

    private UserSearchResponse toSearchResponse(Object[] row) {
        return UserSearchResponse.builder()
                .id(row[0] == null ? null : ((Number) row[0]).longValue())
                .displayName((String) row[1])
                .avatarUrl((String) row[2])
                .level(row[3] == null ? null : ((Number) row[3]).intValue())
                .isFollowing(toBoolean(row[4]))
                .build();
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    private UserProfileResponse toProfileResponse(Long currentUserId, User targetUser) {
        return UserProfileResponse.builder()
                .id(targetUser.getId())
                .displayName(targetUser.getDisplayName())
                .username(targetUser.getUsername())
                .avatarUrl(null)
                .level(targetUser.getLevel())
                .isFollowing(currentUserId != null
                        && targetUser.getId() != null
                    && userRepository.existsFollowRelation(currentUserId, targetUser.getId()) > 0)
                .build();
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