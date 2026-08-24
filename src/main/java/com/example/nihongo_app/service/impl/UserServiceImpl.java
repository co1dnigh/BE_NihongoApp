package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.SyncContactsRequest;
import com.example.nihongo_app.dto.request.UpdatePhoneRequest;
import com.example.nihongo_app.dto.request.UpdateProfileRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.dto.response.AdminSummaryResponse;
import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.dto.response.FollowSummaryResponse;
import com.example.nihongo_app.dto.response.UserOverviewResponse;
import com.example.nihongo_app.dto.response.UserPublicProfileResponse;
import com.example.nihongo_app.dto.response.UserSearchResponse;
import com.example.nihongo_app.dto.response.UserProfileResponse;
import com.example.nihongo_app.dto.response.UserStatsResponse;
import com.example.nihongo_app.dto.response.ActiveEffectResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserActiveEffectRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.EnergyService;
import com.example.nihongo_app.service.UserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EnergyService energyService;
    private final UserActiveEffectRepository userActiveEffectRepository;

    @Override
    @Transactional
    public UserStatsResponse getMyStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Hoi nang luong thu dong truoc khi doc, de currentEnergy luon chinh xac ngay ca khi
        // FE chi goi endpoint nay ma khong goi rieng GET /energy.
        if (user.getId() != null) {
            energyService.recoverEnergy(user.getId());
        }

        List<ActiveEffectResponse> activeEffects = userActiveEffectRepository.findByUserIdAndExpiresAtAfter(user.getId(), LocalDateTime.now())
                .stream()
                .map(effect -> ActiveEffectResponse.builder()
                        .effectType(effect.getEffectType().name())
                        .expiresAt(effect.getExpiresAt())
                        .build())
                .collect(Collectors.toList());

        return UserStatsResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .username(user.getUsername())
                .role(user.getRole())
                .level(user.getLevel())
                .exp(user.getExp())
                .rankId(user.getRank() != null ? user.getRank().getId() : null)
                .rankName(user.getRank() != null ? user.getRank().getName() : null)
                .coins(user.getCoins())
                .currentEnergy(user.getCurrentEnergy())
                .maxEnergy(user.getMaxEnergy())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .streakFreezeCount(user.getStreakFreezeCount())
                .activeEffects(activeEffects)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSummaryResponse> getAllUsers() {
        return userRepository.findAllByDeletedAtIsNull().stream()
                .map(this::toAdminSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSummaryResponse getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toAdminSummaryResponse(user);
    }

    @Override
    @Transactional
    public AdminSummaryResponse updateUser(Long id, UpdateProfileRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String username = request.getUsername().trim();
        if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        user.setDisplayName(request.getDisplayName().trim());
        user.setUsername(username);
        return toAdminSummaryResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setDeletedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

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
                .avatarUrl(user.getAvatarUrl())
                .level(user.getLevel())
                .build();
    }

    private AdminSummaryResponse toAdminSummaryResponse(User user) {
        return AdminSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
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
                .avatarUrl(targetUser.getAvatarUrl())
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
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String getAvatarUrl(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getAvatarUrl();
    }

    @Override
    @Transactional
    public void updateAvatarUrl(String currentUserEmail, String avatarUrl) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    // ============================ Follow (module Social Feed) ============================

    @Override
    @Transactional
    public void followUser(Long currentUserId, Long targetUserId) {
        if (Objects.equals(currentUserId, targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }
        if (userRepository.existsFollowRelation(currentUserId, targetUserId) > 0) {
            return; // da follow roi -> idempotent, khong lam gi them
        }
        try {
            userRepository.insertFollow(currentUserId, targetUserId);
        } catch (DataIntegrityViolationException ex) {
            // Race condition: 2 request follow gan nhu dong thoi, 1 request da insert truoc ->
            // coi nhu thanh cong (dung y muon idempotent), khong nem loi cho client.
        }
    }

    @Override
    @Transactional
    public void unfollowUser(Long currentUserId, Long targetUserId) {
        // Xoa dong khong ton tai la no-op tu nhien trong SQL -> tu idempotent, khong can check truoc.
        userRepository.deleteFollow(currentUserId, targetUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPublicProfileResponse getPublicProfile(Long currentUserId, Long targetUserId) {
        List<Object[]> rows = userRepository.findUserProfileDetail(targetUserId, currentUserId);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        Object[] row = rows.get(0);
        return UserPublicProfileResponse.builder()
                .id(((Number) row[0]).longValue())
                .displayName((String) row[1])
                .avatarUrl((String) row[2])
                .rankName((String) row[3])
                .currentStreak(row[4] == null ? null : ((Number) row[4]).intValue())
                .followerCount(((Number) row[5]).longValue())
                .followingCount(((Number) row[6]).longValue())
                .isFollowing(toBoolean(row[7]))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FollowSummaryResponse> getFollowers(Long targetUserId, Long currentUserId,
                                                                   String cursor, int size) {
        String[] parts = CursorCodec.decode(cursor);
        LocalDateTime cursorTime = parts == null ? null : LocalDateTime.parse(parts[0]);
        Long cursorId = parts == null ? null : Long.valueOf(parts[1]);

        Pageable pageable = PageRequest.of(0, size + 1);
        List<Object[]> rows = userRepository.findFollowersCursor(targetUserId, currentUserId,
                cursorTime, cursorId, pageable);
        return toFollowSummaryPage(rows, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FollowSummaryResponse> getFollowing(Long targetUserId, Long currentUserId,
                                                                   String cursor, int size) {
        String[] parts = CursorCodec.decode(cursor);
        LocalDateTime cursorTime = parts == null ? null : LocalDateTime.parse(parts[0]);
        Long cursorId = parts == null ? null : Long.valueOf(parts[1]);

        Pageable pageable = PageRequest.of(0, size + 1);
        List<Object[]> rows = userRepository.findFollowingCursor(targetUserId, currentUserId,
                cursorTime, cursorId, pageable);
        return toFollowSummaryPage(rows, size);
    }

    /** Moi hang: [id, display_name, avatar_url, followed_since, is_following]. */
    private CursorPageResponse<FollowSummaryResponse> toFollowSummaryPage(List<Object[]> rows, int size) {
        boolean hasMore = rows.size() > size;
        List<Object[]> pageRows = hasMore ? rows.subList(0, size) : rows;

        List<FollowSummaryResponse> items = new ArrayList<>(pageRows.size());
        for (Object[] row : pageRows) {
            items.add(FollowSummaryResponse.builder()
                    .id(((Number) row[0]).longValue())
                    .displayName((String) row[1])
                    .avatarUrl((String) row[2])
                    .isFollowing(toBoolean(row[4]))
                    .build());
        }

        String nextCursor = null;
        if (hasMore) {
            Object[] lastRow = pageRows.get(pageRows.size() - 1);
            LocalDateTime lastTime = toLocalDateTime(lastRow[3]);
            Long lastId = ((Number) lastRow[0]).longValue();
            nextCursor = CursorCodec.encode(lastTime.toString(), lastId.toString());
        }
        return CursorPageResponse.<FollowSummaryResponse>builder().items(items).nextCursor(nextCursor).build();
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<UserSearchResponse> searchUsersCursor(Long currentUserId, String keyword,
                                                                     String cursor, int size) {
        String searchKeyword = keyword == null ? "" : keyword.trim();
        String[] parts = CursorCodec.decode(cursor);
        String cursorName = parts == null ? null : parts[0];
        Long cursorId = parts == null ? null : Long.valueOf(parts[1]);

        Pageable pageable = PageRequest.of(0, size + 1);
        List<Object[]> rows = userRepository.searchUsersCursor(currentUserId, searchKeyword,
                cursorName, cursorId, pageable);

        boolean hasMore = rows.size() > size;
        List<Object[]> pageRows = hasMore ? rows.subList(0, size) : rows;

        List<UserSearchResponse> items = pageRows.stream().map(this::toSearchResponse).toList();

        String nextCursor = null;
        if (hasMore) {
            Object[] lastRow = pageRows.get(pageRows.size() - 1);
            nextCursor = CursorCodec.encode((String) lastRow[1], String.valueOf(((Number) lastRow[0]).longValue()));
        }
        return CursorPageResponse.<UserSearchResponse>builder().items(items).nextCursor(nextCursor).build();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime dt) {
            return dt;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        throw new IllegalStateException("Khong the doc created_at tu ket qua query: " + value);
    }
}