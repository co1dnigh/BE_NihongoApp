package com.example.nihongo_app.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CommentResponse {
    Long id;
    PostAuthorResponse author;
    String content;
    LocalDateTime createdAt;
}
