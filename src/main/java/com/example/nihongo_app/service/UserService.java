package com.example.nihongo_app.service;

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
import java.util.List;

public interface UserService {

    UserStatsResponse getMyStats(String email);

    List<AdminSummaryResponse> getAllUsers();
    AdminSummaryResponse getUserById(Long id);
    AdminSummaryResponse updateUser(Long id, UpdateProfileRequest request);
    void deleteUser(Long id);

    void updatePhoneNumber(String email, UpdatePhoneRequest request);

    List<UserOverviewResponse> findFriendsFromContacts(SyncContactsRequest request);

    boolean toggleFollow(String currentUserEmail, Long targetUserId);

    List<UserSearchResponse> searchUsers(String currentUserEmail, String keyword);

    UserProfileResponse getProfileByUsername(String currentUserEmail, String targetUsername);

    AuthResponse updateProfile(String currentUserEmail, UpdateProfileRequest request);

    String getAvatarUrl(String currentUserEmail);

    void updateAvatarUrl(String currentUserEmail, String avatarUrl);

    // ============================ Follow (module Social Feed) ============================
    // Cac method moi ben duoi dung Long userId (tu AppUserPrincipal) thay vi email nhu cac
    // method o tren (pattern cu) - tranh 1 query findByEmail thua cho moi request, dung
    // dung pattern cac controller/service moi (Mistake, Shop, Rank, Achievement) dang dung.

    /** Follow idempotent — da follow roi thi khong lam gi, tu follow bi chan. */
    void followUser(Long currentUserId, Long targetUserId);

    /** Unfollow idempotent — chua follow thi khong lam gi, khong loi. */
    void unfollowUser(Long currentUserId, Long targetUserId);

    UserPublicProfileResponse getPublicProfile(Long currentUserId, Long targetUserId);

    CursorPageResponse<FollowSummaryResponse> getFollowers(Long targetUserId, Long currentUserId,
                                                           String cursor, int size);

    CursorPageResponse<FollowSummaryResponse> getFollowing(Long targetUserId, Long currentUserId,
                                                           String cursor, int size);

    CursorPageResponse<UserSearchResponse> searchUsersCursor(Long currentUserId, String keyword,
                                                             String cursor, int size);
}