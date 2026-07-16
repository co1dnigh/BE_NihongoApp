package com.example.nihongo_app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    @Default
    private String tokenType = "Bearer";

    private UserDto user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {

        private Long id;
        private String email;
        private String displayName;
        private String username;
        private String role;
    }
}