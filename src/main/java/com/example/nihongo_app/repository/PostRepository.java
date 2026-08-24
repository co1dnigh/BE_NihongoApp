package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Feed = bài của {@code authorIds} (chính mình + đang follow), cursor-based theo
     * (created_at, id) giảm dần. {@code cursorTime}/{@code cursorId} truyền {@code null}
     * cho trang đầu tiên. {@code pageable} chỉ dùng để giới hạn số dòng (LIMIT do Spring Data
     * tự thêm) — truyền {@code PageRequest.of(0, size + 1)} (lấy dư 1 dòng để biết còn trang
     * sau hay không mà không cần query COUNT riêng).
     */
    @Query(value = "SELECT * FROM posts "
            + "WHERE deleted_at IS NULL AND user_id IN (:authorIds) "
            + "AND (:cursorTime IS NULL OR created_at < :cursorTime "
            + "     OR (created_at = :cursorTime AND id < :cursorId)) "
            + "ORDER BY created_at DESC, id DESC", nativeQuery = true)
    List<Post> findFeed(@Param("authorIds") List<Long> authorIds,
                        @Param("cursorTime") LocalDateTime cursorTime,
                        @Param("cursorId") Long cursorId,
                        Pageable pageable);

    /** Đếm like theo từng post trong 1 lần query (chống N+1) — mỗi hàng: [post_id, count]. */
    @Query(value = "SELECT post_id, COUNT(*) FROM post_likes WHERE post_id IN (:postIds) GROUP BY post_id",
            nativeQuery = true)
    List<Object[]> countLikesByPostIds(@Param("postIds") List<Long> postIds);

    /** Đếm comment (chưa xoá mềm) theo từng post trong 1 lần query — mỗi hàng: [post_id, count]. */
    @Query(value = "SELECT post_id, COUNT(*) FROM post_comments "
            + "WHERE post_id IN (:postIds) AND deleted_at IS NULL GROUP BY post_id",
            nativeQuery = true)
    List<Object[]> countCommentsByPostIds(@Param("postIds") List<Long> postIds);

    /** Danh sách post_id mà {@code userId} đã like, giới hạn trong {@code postIds} — 1 query cho cả trang. */
    @Query(value = "SELECT post_id FROM post_likes WHERE post_id IN (:postIds) AND user_id = :userId",
            nativeQuery = true)
    List<Long> findLikedPostIds(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);
}
