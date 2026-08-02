package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByDeletedAtIsNull();

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findByEmail(String email);

    // 1. THÊM MỚI: Dùng cho tính năng quét mã QR (Tìm user theo username)
    Optional<User> findByUsername(String username);

    // 2. THÊM MỚI: Dùng để kiểm tra xem username đã có ai dùng chưa lúc cập nhật Profile
    boolean existsByUsername(String username);

    List<User> findByPhoneNumberIn(List<String> phoneNumbers);

    @Query(value = "SELECT CASE WHEN EXISTS (SELECT 1 FROM user_follows WHERE follower_id = :followerId AND followed_id = :followedId) THEN 1 ELSE 0 END", nativeQuery = true)
    int existsFollowRelation(@Param("followerId") Long followerId,
                             @Param("followedId") Long followedId);

    @Modifying
    @Query(value = "INSERT INTO user_follows (follower_id, followed_id, created_at) VALUES (:followerId, :followedId, CURRENT_TIMESTAMP)", nativeQuery = true)
    int insertFollow(@Param("followerId") Long followerId,
                     @Param("followedId") Long followedId);

    @Modifying
    @Query(value = "DELETE FROM user_follows WHERE follower_id = :followerId AND followed_id = :followedId", nativeQuery = true)
    int deleteFollow(@Param("followerId") Long followerId,
                     @Param("followedId") Long followedId);

    // 3. CẬP NHẬT: Câu query quét "3 trong 1" (Tìm theo display_name HOẶC username HOẶC email)
    @Query(value = """
            SELECT
                u.id,
                u.display_name,
                u.avatar_url,
                u.level,
                CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM user_follows uf
                        WHERE uf.follower_id = :currentUserId
                          AND uf.followed_id = u.id
                    ) THEN 1
                    ELSE 0
                END AS is_following
            FROM users u
            WHERE u.deleted_at IS NULL
              AND u.id <> :currentUserId
              AND (
                  LOWER(u.display_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY u.display_name
            """, nativeQuery = true)
    List<Object[]> searchUsers(@Param("currentUserId") Long currentUserId,
                               @Param("keyword") String keyword);
}