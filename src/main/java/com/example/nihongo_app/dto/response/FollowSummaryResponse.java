package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

/** 1 dòng trong danh sách followers/following — {@code isFollowing} để FE hiện nút "follow back". */
@Value
@Builder
public class FollowSummaryResponse {
    Long id;
    String displayName;
    String avatarUrl;
    boolean isFollowing;
}
