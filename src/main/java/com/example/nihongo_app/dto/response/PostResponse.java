package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.Post.PostType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

/**
 * 1 bài đăng trong feed. {@code likeCount}/{@code commentCount}/{@code likedByMe} được
 * {@code FeedServiceImpl} tính bằng query gộp cho cả trang (chống N+1), không tính riêng
 * từng post.
 */
@Value
@Builder
public class PostResponse {

    Long id;
    PostAuthorResponse author;
    PostType postType;
    String content;
    LocalDateTime createdAt;
    long likeCount;
    long commentCount;
    boolean likedByMe;
}
