package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.PostComment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    Optional<PostComment> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Danh sách comment của 1 post, cursor-based theo (created_at, id) giảm dần (mới nhất
     * trước, nhất quán với hướng cursor của feed/followers/following trong module này).
     */
    @Query(value = "SELECT * FROM post_comments "
            + "WHERE post_id = :postId AND deleted_at IS NULL "
            + "AND (:cursorTime IS NULL OR created_at < :cursorTime "
            + "     OR (created_at = :cursorTime AND id < :cursorId)) "
            + "ORDER BY created_at DESC, id DESC", nativeQuery = true)
    List<PostComment> findByPostIdCursor(@Param("postId") Long postId,
                                         @Param("cursorTime") LocalDateTime cursorTime,
                                         @Param("cursorId") Long cursorId,
                                         Pageable pageable);
}
