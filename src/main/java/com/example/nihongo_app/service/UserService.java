package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.SyncContactsRequest;
import com.example.nihongo_app.dto.request.UpdatePhoneRequest;
import com.example.nihongo_app.dto.request.UpdateProfileRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.dto.response.AdminSummaryResponse;
import com.example.nihongo_app.dto.response.UserOverviewResponse;
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
}