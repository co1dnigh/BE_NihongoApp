package com.example.nihongo_app.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test tích hợp module Social Feed chạy qua tầng HTTP thật (MockMvc + Spring Security thật +
 * MySQL thật) — mục đích chính là xác nhận {@code ResponseStatusException} ném từ service
 * (self-follow, xoá post người khác...) thực sự đi ra đúng HTTP status qua
 * {@code GlobalExceptionHandler}, chứ không chỉ verify ở tầng service bằng Mockito (mock không
 * đi qua {@code GlobalExceptionHandler} nên không phát hiện được nếu handler nào đó "nuốt"
 * nhầm exception thành 500).
 *
 * <p>{@code @Transactional} ở class giúp rollback tự động sau mỗi test, không làm bẩn DB dev.</p>
 */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class SocialFeedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.builder()
                .email("sf-a-" + UUID.randomUUID() + "@test.com")
                .role("LEARNER").displayName("Social A").build());
        userB = userRepository.save(User.builder()
                .email("sf-b-" + UUID.randomUUID() + "@test.com")
                .role("LEARNER").displayName("Social B").build());
        tokenA = "Bearer " + jwtTokenProvider.generateToken(userA);
        tokenB = "Bearer " + jwtTokenProvider.generateToken(userB);
    }

    @Test
    void followSelf_returns400NotServerError() throws Exception {
        mockMvc.perform(put("/api/v1/users/" + userA.getId() + "/follow")
                        .header("Authorization", tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void followUnfollow_idempotentReturns204() throws Exception {
        mockMvc.perform(put("/api/v1/users/" + userB.getId() + "/follow")
                        .header("Authorization", tokenA))
                .andExpect(status().isNoContent());
        // Goi lai lan 2 (da follow roi) van phai 204, khong duoc loi.
        mockMvc.perform(put("/api/v1/users/" + userB.getId() + "/follow")
                        .header("Authorization", tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/users/" + userB.getId() + "/follow")
                        .header("Authorization", tokenA))
                .andExpect(status().isNoContent());
        // Unfollow lan 2 (da khong follow) van phai 204.
        mockMvc.perform(delete("/api/v1/users/" + userB.getId() + "/follow")
                        .header("Authorization", tokenA))
                .andExpect(status().isNoContent());
    }

    @Test
    void feed_showsFollowedUsersPost_withCorrectLikeCommentCounts() throws Exception {
        mockMvc.perform(put("/api/v1/users/" + userB.getId() + "/follow")
                        .header("Authorization", tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Xin chao tu B\"}"))
                .andExpect(status().isCreated());

        // A dang follow B -> feed cua A phai thay bai cua B.
        mockMvc.perform(get("/api/v1/feed").header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].content").value("Xin chao tu B"))
                .andExpect(jsonPath("$.items[0].author.id").value(userB.getId()))
                .andExpect(jsonPath("$.items[0].likeCount").value(0))
                .andExpect(jsonPath("$.items[0].likedByMe").value(false));

        Long postId = extractFirstPostId();

        // Like 2 lan lien tiep (double-tap) -> khong loi, khong tang doi.
        mockMvc.perform(post("/api/v1/posts/" + postId + "/like").header("Authorization", tokenA))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/posts/" + postId + "/like").header("Authorization", tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/feed").header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].likeCount").value(1))
                .andExpect(jsonPath("$.items[0].likedByMe").value(true));
    }

    @Test
    void deletePost_notOwner_returns403NotServerError() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Bai cua B\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/users/" + userB.getId() + "/follow")
                        .header("Authorization", tokenA))
                .andExpect(status().isNoContent());
        Long postId = extractFirstPostId();

        // A khong phai chu bai -> xoa phai bi tu choi 403, khong duoc 500.
        mockMvc.perform(delete("/api/v1/posts/" + postId).header("Authorization", tokenA))
                .andExpect(status().isForbidden());

        // Chu that su (B) xoa duoc.
        mockMvc.perform(delete("/api/v1/posts/" + postId).header("Authorization", tokenB))
                .andExpect(status().isNoContent());
    }

    /**
     * Lay id cua post dau tien trong feed. Dung Jackson doc dung field "id" o cap top-level
     * cua item (khong duoc dung regex "\"id\":\\d+" don gian vi Jackson serialize field theo
     * thu tu alphabet -> "author":{"id":...} co the dung TRUOC "id" cua chinh post, regex se
     * bat nham id cua author).
     */
    private Long extractFirstPostId() throws Exception {
        String body = mockMvc.perform(get("/api/v1/feed").header("Authorization", tokenA))
                .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        return root.get("items").get(0).get("id").asLong();
    }
}
