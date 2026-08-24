package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByDeletedAtIsNull();

    List<User> findAllByDeletedAtIsNullAndLastLearningAtIsNotNull();

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

    // "exp" la ten ham dung san (EXP - ham mu) trong grammar HQL cua Hibernate 6/7, nen viet
    // u.exp trong JPQL se loi "no viable alternative at input 'u.exp'" (kieu escape bang backtick
    // cung khong duoc chap nhan o day) -> dung native SQL de tranh han HQL parser.
    @Query(value = "SELECT * FROM users WHERE deleted_at IS NULL AND rank_id = :rankId "
            + "ORDER BY exp DESC LIMIT 15", nativeQuery = true)
    List<User> findTop15ByRankIdOrderByExpDesc(@Param("rankId") Long rankId);

    @Query(value = "SELECT COUNT(*) FROM users WHERE deleted_at IS NULL AND rank_id = :rankId "
            + "AND exp > :exp", nativeQuery = true)
    Long countUsersWithExpGreaterThanInRank(@Param("rankId") Long rankId,
                                            @Param("exp") Integer exp);

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

    // ============================ Follow (module Social Feed) ============================
    // Cac method ben duoi la THEM MOI, khong dung chung/khong doi cac method follow o tren
    // (existsFollowRelation/insertFollow/deleteFollow/searchUsers) de khong lam vo API cu.

    @Query(value = "SELECT COUNT(*) FROM user_follows WHERE followed_id = :userId", nativeQuery = true)
    long countFollowers(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM user_follows WHERE follower_id = :userId", nativeQuery = true)
    long countFollowing(@Param("userId") Long userId);

    /** Danh sach followed_id cua 1 user — dung de FeedService gom authorIds (chinh minh + dang follow). */
    @Query(value = "SELECT followed_id FROM user_follows WHERE follower_id = :userId", nativeQuery = true)
    List<Long> findFollowingIds(@Param("userId") Long userId);

    /**
     * Danh sach nguoi dang follow {@code userId} (followers), cursor theo thoi diem follow.
     * Moi hang: [id, display_name, avatar_url, is_following] (is_following = currentUser co
     * dang follow nguoi nay khong, de FE hien nut "follow back").
     */
    @Query(value = """
            SELECT u.id, u.display_name, u.avatar_url, uf.created_at AS followed_since,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM user_follows uf2
                       WHERE uf2.follower_id = :currentUserId AND uf2.followed_id = u.id
                   ) THEN 1 ELSE 0 END AS is_following
            FROM user_follows uf
            JOIN users u ON u.id = uf.follower_id
            WHERE uf.followed_id = :userId
              AND u.deleted_at IS NULL
              AND (:cursorTime IS NULL OR uf.created_at < :cursorTime
                   OR (uf.created_at = :cursorTime AND uf.follower_id < :cursorId))
            ORDER BY uf.created_at DESC, uf.follower_id DESC
            """, nativeQuery = true)
    List<Object[]> findFollowersCursor(@Param("userId") Long userId,
                                       @Param("currentUserId") Long currentUserId,
                                       @Param("cursorTime") LocalDateTime cursorTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    /** Danh sach nguoi ma {@code userId} dang follow (following), cursor theo thoi diem follow. */
    @Query(value = """
            SELECT u.id, u.display_name, u.avatar_url, uf.created_at AS followed_since,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM user_follows uf2
                       WHERE uf2.follower_id = :currentUserId AND uf2.followed_id = u.id
                   ) THEN 1 ELSE 0 END AS is_following
            FROM user_follows uf
            JOIN users u ON u.id = uf.followed_id
            WHERE uf.follower_id = :userId
              AND u.deleted_at IS NULL
              AND (:cursorTime IS NULL OR uf.created_at < :cursorTime
                   OR (uf.created_at = :cursorTime AND uf.followed_id < :cursorId))
            ORDER BY uf.created_at DESC, uf.followed_id DESC
            """, nativeQuery = true)
    List<Object[]> findFollowingCursor(@Param("userId") Long userId,
                                       @Param("currentUserId") Long currentUserId,
                                       @Param("cursorTime") LocalDateTime cursorTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    /**
     * Ho so cong khai theo id: 1 query gom ca followerCount/followingCount/rank/isFollowing,
     * tranh N+1. Hang tra ve: [id, display_name, avatar_url, rank_name, current_streak,
     * follower_count, following_count, is_following].
     */
    /**
     * Ban cursor-paginated cua searchUsers o tren (KHONG doi query cu) — sap theo display_name
     * tang dan, tie-break bang id (id cung dung lam thanh phan cursor thu 2).
     */
    @Query(value = """
            SELECT u.id, u.display_name, u.avatar_url, u.level,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM user_follows uf
                       WHERE uf.follower_id = :currentUserId AND uf.followed_id = u.id
                   ) THEN 1 ELSE 0 END AS is_following
            FROM users u
            WHERE u.deleted_at IS NULL
              AND u.id <> :currentUserId
              AND (
                  LOWER(u.display_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
              AND (:cursorName IS NULL OR u.display_name > :cursorName
                   OR (u.display_name = :cursorName AND u.id > :cursorId))
            ORDER BY u.display_name ASC, u.id ASC
            """, nativeQuery = true)
    List<Object[]> searchUsersCursor(@Param("currentUserId") Long currentUserId,
                                     @Param("keyword") String keyword,
                                     @Param("cursorName") String cursorName,
                                     @Param("cursorId") Long cursorId,
                                     Pageable pageable);

    @Query(value = """
            SELECT u.id, u.display_name, u.avatar_url, r.name AS rank_name, u.current_streak,
                   (SELECT COUNT(*) FROM user_follows WHERE followed_id = u.id) AS follower_count,
                   (SELECT COUNT(*) FROM user_follows WHERE follower_id = u.id) AS following_count,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM user_follows WHERE follower_id = :currentUserId AND followed_id = u.id
                   ) THEN 1 ELSE 0 END AS is_following
            FROM users u
            LEFT JOIN ranks r ON r.id = u.rank_id
            WHERE u.id = :targetUserId AND u.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> findUserProfileDetail(@Param("targetUserId") Long targetUserId,
                                         @Param("currentUserId") Long currentUserId);
}