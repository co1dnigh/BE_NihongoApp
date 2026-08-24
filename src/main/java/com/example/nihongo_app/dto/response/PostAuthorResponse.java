package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostAuthorResponse {
    Long id;
    String displayName;
    String avatarUrl;
}
