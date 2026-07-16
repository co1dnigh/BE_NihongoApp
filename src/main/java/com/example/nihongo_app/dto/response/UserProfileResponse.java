package com.example.nihongo_app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String displayName;
    private String username;
    private String avatarUrl;
    private Integer level;

    @JsonProperty("isFollowing")
    private boolean isFollowing;
}