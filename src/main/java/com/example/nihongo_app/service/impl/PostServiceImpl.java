package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.response.CommentResponse;
import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.dto.response.PostAuthorResponse;
import com.example.nihongo_app.entity.Achievement;
import com.example.nihongo_app.entity.Post;
import com.example.nihongo_app.entity.PostComment;
import com.example.nihongo_app.entity.PostLike;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.PostCommentRepository;
import com.example.nihongo_app.repository.PostLikeRepository;
import com.example.nihongo_app.repository.PostRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.PostService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final int MAX_STATUS_CONTENT_LENGTH = 500;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createUserStatusPost(Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }
        if (content.length() > MAX_STATUS_CONTENT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Content vuot qua " + MAX_STATUS_CONTENT_LENGTH + " ky tu");
        }
        postRepository.save(Post.builder()
                .userId(userId)
                .content(content.trim())
                .postType(Post.PostType.USER_STATUS)
                .build());
    }

    @Override
    @Transactional
    public void createSystemAchievementPost(Long userId, Achievement achievement) {
        // Khong validate do nay den tu he Achievement noi bo, khong phai input tu FE.
        String content = "Da dat thanh tich \"" + achievement.getName() + "\"!"
                + (achievement.getDescription() == null || achievement.getDescription().isBlank()
                        ? "" : " " + achievement.getDescription());
        postRepository.save(Post.builder()
                .userId(userId)
                .content(content)
                .postType(Post.PostType.SYSTEM_ACHIEVEMENT)
                .build());
    }

    @Override
    @Transactional
    public void deletePost(Long currentUserId, Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        assertOwnerOrAdmin(post.getUserId(), currentUserId);
        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void likePost(Long currentUserId, Long postId) {
        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (postLikeRepository.existsByUserIdAndPostId(currentUserId, postId)) {
            return; // da like roi -> idempotent
        }
        try {
            postLikeRepository.save(PostLike.builder().userId(currentUserId).postId(postId).build());
        } catch (DataIntegrityViolationException ex) {
            // Race condition: bam like lien tuc/2 request gan nhu dong thoi -> coi nhu da like,
            // khong nem loi cho client (dung yeu cau idempotent).
            log.debug("Like trung (race condition) user={} post={}", currentUserId, postId);
        }
    }

    @Override
    @Transactional
    public void unlikePost(Long currentUserId, Long postId) {
        postLikeRepository.deleteByUserIdAndPostId(currentUserId, postId);
    }

    @Override
    @Transactional
    public void addComment(Long currentUserId, Long postId, String content) {
        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }
        postCommentRepository.save(PostComment.builder()
                .postId(postId)
                .userId(currentUserId)
                .content(content.trim())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> listComments(Long postId, String cursor, int size) {
        String[] parts = CursorCodec.decode(cursor);
        LocalDateTime cursorTime = parts == null ? null : LocalDateTime.parse(parts[0]);
        Long cursorId = parts == null ? null : Long.valueOf(parts[1]);

        Pageable pageable = PageRequest.of(0, size + 1);
        List<PostComment> rows = postCommentRepository.findByPostIdCursor(postId, cursorTime, cursorId, pageable);

        boolean hasMore = rows.size() > size;
        List<PostComment> pageRows = hasMore ? rows.subList(0, size) : rows;

        // Gom author 1 lan cho ca trang, tranh N+1 (giong cach FeedServiceImpl lam voi post).
        List<Long> authorIds = pageRows.stream().map(PostComment::getUserId).distinct().toList();
        Map<Long, User> authorsById = new HashMap<>();
        for (User u : userRepository.findAllById(authorIds)) {
            authorsById.put(u.getId(), u);
        }

        List<CommentResponse> items = new ArrayList<>(pageRows.size());
        for (PostComment c : pageRows) {
            User author = authorsById.get(c.getUserId());
            items.add(CommentResponse.builder()
                    .id(c.getId())
                    .author(PostAuthorResponse.builder()
                            .id(c.getUserId())
                            .displayName(author == null ? null : author.getDisplayName())
                            .avatarUrl(author == null ? null : author.getAvatarUrl())
                            .build())
                    .content(c.getContent())
                    .createdAt(c.getCreatedAt())
                    .build());
        }

        String nextCursor = null;
        if (hasMore) {
            PostComment last = pageRows.get(pageRows.size() - 1);
            nextCursor = CursorCodec.encode(last.getCreatedAt().toString(), last.getId().toString());
        }
        return CursorPageResponse.<CommentResponse>builder().items(items).nextCursor(nextCursor).build();
    }

    @Override
    @Transactional
    public void deleteComment(Long currentUserId, Long commentId) {
        PostComment comment = postCommentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        assertOwnerOrAdmin(comment.getUserId(), currentUserId);
        comment.setDeletedAt(LocalDateTime.now());
        postCommentRepository.save(comment);
    }

    /** Chi chu tai nguyen (ownerId == currentUserId) hoac ADMIN moi duoc thao tac. */
    private void assertOwnerOrAdmin(Long ownerId, Long currentUserId) {
        if (Objects.equals(ownerId, currentUserId)) {
            return;
        }
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ban khong co quyen thao tac tren tai nguyen nay");
        }
    }
}
