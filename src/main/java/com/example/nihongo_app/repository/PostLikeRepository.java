package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLike.PostLikeId> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    /** Xoá idempotent — xoá dòng không tồn tại là no-op tự nhiên trong SQL, không lỗi. */
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
