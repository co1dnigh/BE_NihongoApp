package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.dto.response.PostResponse;
import com.example.nihongo_app.entity.Post;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.PostRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

/**
 * Test {@link FeedServiceImpl}: cursor pagination (đầu/giữa/cuối/rỗng) và chống N+1
 * (đúng 1 query feed + 3 query batch like/comment/likedByMe + 1 query author, không phụ
 * thuộc số lượng post trong trang).
 */
@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long FOLLOWED_ID = 2L;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private FeedServiceImpl feedService;

    @BeforeEach
    void setUp() {
        feedService = new FeedServiceImpl(postRepository, userRepository);
        when(userRepository.findFollowingIds(USER_ID)).thenReturn(List.of(FOLLOWED_ID));
    }

    private Post post(long id, long authorId, LocalDateTime createdAt) {
        return Post.builder()
                .id(id)
                .userId(authorId)
                .content("noi dung " + id)
                .postType(Post.PostType.USER_STATUS)
                .createdAt(createdAt)
                .build();
    }

    private User author(long id) {
        return User.builder().id(id).displayName("User " + id).avatarUrl(null).build();
    }

    /** Trang đầu (không cursor): còn đủ dữ liệu để lấp đầy size + 1 -> nextCursor khác null. */
    @Test
    void getFeed_firstPage_noCursor_returnsNextCursorWhenMoreDataExists() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> rows = new ArrayList<>();
        for (int i = 3; i >= 1; i--) {
            rows.add(post(i, USER_ID, now.minusMinutes(3 - i)));
        }
        // size = 2 -> repository duoc goi voi PageRequest(0, 3), gia lap tra du 3 dong (co "trang sau").
        when(postRepository.findFeed(eq(List.of(FOLLOWED_ID, USER_ID)), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(rows);
        stubBatchQueries(List.of(3L, 2L, 1L), List.of(USER_ID));

        CursorPageResponse<PostResponse> result = feedService.getFeed(USER_ID, null, 2);

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getId()).isEqualTo(3L);
        assertThat(result.getNextCursor()).isNotNull();
    }

    /** Trang giữa (có cursor, vẫn còn dữ liệu sau đó) -> nextCursor vẫn khác null. */
    @Test
    void getFeed_middlePage_withCursor_stillHasNextCursor() {
        LocalDateTime now = LocalDateTime.now();
        String cursor = CursorCodec.encode(now.toString(), "5");
        List<Post> rows = List.of(
                post(4, USER_ID, now.minusMinutes(1)),
                post(3, USER_ID, now.minusMinutes(2)),
                post(2, USER_ID, now.minusMinutes(3)));
        when(postRepository.findFeed(eq(List.of(FOLLOWED_ID, USER_ID)), eq(now), eq(5L), any(Pageable.class)))
                .thenReturn(rows);
        stubBatchQueries(List.of(4L, 3L), List.of());

        CursorPageResponse<PostResponse> result = feedService.getFeed(USER_ID, cursor, 2);

        assertThat(result.getItems()).extracting(PostResponse::getId).containsExactly(4L, 3L);
        assertThat(result.getNextCursor()).isNotNull();
    }

    /** Trang cuối: repository trả về ít hơn hoặc bằng size -> hết dữ liệu, nextCursor null. */
    @Test
    void getFeed_lastPage_fewerRowsThanSizePlusOne_nextCursorIsNull() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> rows = List.of(post(1, USER_ID, now.minusMinutes(5)));
        when(postRepository.findFeed(eq(List.of(FOLLOWED_ID, USER_ID)), any(), any(), any(Pageable.class)))
                .thenReturn(rows);
        stubBatchQueries(List.of(1L), List.of(USER_ID));

        CursorPageResponse<PostResponse> result = feedService.getFeed(USER_ID, null, 20);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getNextCursor()).isNull();
    }

    /** Feed rỗng: user không tự đăng gì và không follow ai có bài -> danh sách rỗng, nextCursor null. */
    @Test
    void getFeed_emptyFeed_returnsEmptyItemsAndNullCursor() {
        when(postRepository.findFeed(eq(List.of(FOLLOWED_ID, USER_ID)), any(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        CursorPageResponse<PostResponse> result = feedService.getFeed(USER_ID, null, 20);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getNextCursor()).isNull();
    }

    private void stubBatchQueries(List<Long> likedPostIds, List<Long> authorIds) {
        lenient().when(postRepository.countLikesByPostIds(anyList())).thenReturn(List.of());
        lenient().when(postRepository.countCommentsByPostIds(anyList())).thenReturn(List.of());
        lenient().when(postRepository.findLikedPostIds(anyList(), eq(USER_ID))).thenReturn(likedPostIds);
        lenient().when(userRepository.findAllById(anyList()))
                .thenAnswer(invocation -> {
                    List<Long> ids = invocation.getArgument(0);
                    List<User> users = new ArrayList<>();
                    for (Long id : ids) {
                        users.add(author(id));
                    }
                    return users;
                });
    }
}
