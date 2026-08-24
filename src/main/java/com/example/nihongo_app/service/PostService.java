package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.CommentResponse;
import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.entity.Achievement;

/**
 * Đăng bài, like, comment trong bản tin cộng đồng. {@link #createSystemAchievementPost}
 * được {@code AchievementServiceImpl} gọi tới ngay lúc unlock 1 achievement — xem
 * {@code AchievementServiceImpl.upsertProgress}.
 */
public interface PostService {

    void createUserStatusPost(Long userId, String content);

    /** Tạo post SYSTEM_ACHIEVEMENT — gọi từ AchievementServiceImpl lúc vừa unlock. */
    void createSystemAchievementPost(Long userId, Achievement achievement);

    /** Soft delete — chỉ chủ post hoặc ADMIN. */
    void deletePost(Long currentUserId, Long postId);

    /** Idempotent — like lại lần nữa không lỗi, không tăng thêm. */
    void likePost(Long currentUserId, Long postId);

    /** Idempotent — chưa like mà unlike thì không lỗi. */
    void unlikePost(Long currentUserId, Long postId);

    void addComment(Long currentUserId, Long postId, String content);

    CursorPageResponse<CommentResponse> listComments(Long postId, String cursor, int size);

    /** Soft delete — chỉ chủ comment hoặc ADMIN. */
    void deleteComment(Long currentUserId, Long commentId);
}
