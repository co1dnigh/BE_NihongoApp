package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Hồ sơ công khai của 1 user, tra theo {@code id} (khác {@link UserProfileResponse} tra theo
 * {@code username} đã có sẵn). CHỈ chứa thông tin công khai — không có email/energy/coins.
 */
@Value
@Builder
public class UserPublicProfileResponse {
    Long id;
    String displayName;
    String avatarUrl;
    String rankName;
    Integer currentStreak;
    long followerCount;
    long followingCount;
    boolean isFollowing;
}
