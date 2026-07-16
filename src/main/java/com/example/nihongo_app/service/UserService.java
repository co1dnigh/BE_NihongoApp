package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.SyncContactsRequest;
import com.example.nihongo_app.dto.request.UpdatePhoneRequest;
import com.example.nihongo_app.dto.request.UpdateProfileRequest;
import com.example.nihongo_app.dto.response.AuthResponse;
import com.example.nihongo_app.dto.response.UserOverviewResponse;
import com.example.nihongo_app.dto.response.UserSearchResponse;
import com.example.nihongo_app.dto.response.UserProfileResponse;
import java.util.List;

public interface UserService {

    void updatePhoneNumber(String email, UpdatePhoneRequest request);

    List<UserOverviewResponse> findFriendsFromContacts(SyncContactsRequest request);

    boolean toggleFollow(String currentUserEmail, Long targetUserId);

    List<UserSearchResponse> searchUsers(String currentUserEmail, String keyword);

    UserProfileResponse getProfileByUsername(String currentUserEmail, String targetUsername);

    AuthResponse updateProfile(String currentUserEmail, UpdateProfileRequest request);
}