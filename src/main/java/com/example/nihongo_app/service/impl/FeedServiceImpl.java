package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.dto.response.PostAuthorResponse;
import com.example.nihongo_app.dto.response.PostResponse;
import com.example.nihongo_app.entity.Post;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.PostRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.FeedService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Feed = bài của chính mình + đang follow, mới nhất trước, cursor-based.
 *
 * <p>Chống N+1 (yêu cầu bắt buộc): sau khi có trang {@code Post}, chỉ chạy đúng 3 query
 * gộp (like count / comment count / liked-by-me, mỗi cái {@code GROUP BY post_id IN (:ids)}
 * hoặc {@code IN (:ids)}) + 1 query gộp lấy author — hoàn toàn không phụ thuộc kích thước
 * trang, thay vì query riêng cho từng post.</p>
 */
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> getFeed(Long userId, String cursor, int size) {
        List<Long> authorIds = new ArrayList<>(userRepository.findFollowingIds(userId));
        authorIds.add(userId); // feed bao gom ca bai cua chinh minh

        String[] parts = CursorCodec.decode(cursor);
        LocalDateTime cursorTime = parts == null ? null : LocalDateTime.parse(parts[0]);
        Long cursorId = parts == null ? null : Long.valueOf(parts[1]);

        // Lay du 1 dong so voi size that de biet con trang sau hay khong, khong can query COUNT rieng.
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Post> rows = postRepository.findFeed(authorIds, cursorTime, cursorId, pageable);

        boolean hasMore = rows.size() > size;
        List<Post> pagePosts = hasMore ? rows.subList(0, size) : rows;

        if (pagePosts.isEmpty()) {
            return CursorPageResponse.<PostResponse>builder().items(List.of()).nextCursor(null).build();
        }

        List<Long> postIds = pagePosts.stream().map(Post::getId).toList();
        List<Long> postAuthorIds = pagePosts.stream().map(Post::getUserId).distinct().toList();

        Map<Long, Long> likeCounts = toCountMap(postRepository.countLikesByPostIds(postIds));
        Map<Long, Long> commentCounts = toCountMap(postRepository.countCommentsByPostIds(postIds));
        Set<Long> likedByMe = new HashSet<>(postRepository.findLikedPostIds(postIds, userId));

        Map<Long, User> authorsById = new HashMap<>();
        for (User u : userRepository.findAllById(postAuthorIds)) {
            authorsById.put(u.getId(), u);
        }

        List<PostResponse> items = new ArrayList<>(pagePosts.size());
        for (Post p : pagePosts) {
            User author = authorsById.get(p.getUserId());
            items.add(PostResponse.builder()
                    .id(p.getId())
                    .author(PostAuthorResponse.builder()
                            .id(p.getUserId())
                            .displayName(author == null ? null : author.getDisplayName())
                            .avatarUrl(author == null ? null : author.getAvatarUrl())
                            .build())
                    .postType(p.getPostType())
                    .content(p.getContent())
                    .createdAt(p.getCreatedAt())
                    .likeCount(likeCounts.getOrDefault(p.getId(), 0L))
                    .commentCount(commentCounts.getOrDefault(p.getId(), 0L))
                    .likedByMe(likedByMe.contains(p.getId()))
                    .build());
        }

        String nextCursor = null;
        if (hasMore) {
            Post last = pagePosts.get(pagePosts.size() - 1);
            nextCursor = CursorCodec.encode(last.getCreatedAt().toString(), last.getId().toString());
        }
        return CursorPageResponse.<PostResponse>builder().items(items).nextCursor(nextCursor).build();
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return map;
    }
}
