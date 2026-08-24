package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 1 lượt like của user cho 1 post — bảng {@code post_likes} (composite PK
 * {@code user_id, post_id} đã có sẵn từ {@code V5}, chống like trùng ở tầng DB).
 */
@Entity
@Table(name = "post_likes")
@IdClass(PostLike.PostLikeId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Khoá composite tương ứng {@code @IdClass} — bắt buộc equals/hashCode đúng theo field PK. */
    public static class PostLikeId implements Serializable {
        private Long userId;
        private Long postId;

        public PostLikeId() {
        }

        public PostLikeId(Long userId, Long postId) {
            this.userId = userId;
            this.postId = postId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PostLikeId that)) return false;
            return Objects.equals(userId, that.userId) && Objects.equals(postId, that.postId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, postId);
        }
    }
}
