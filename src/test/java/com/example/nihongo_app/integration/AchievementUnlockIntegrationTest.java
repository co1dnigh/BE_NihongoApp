package com.example.nihongo_app.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.Topic;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.TopicRepository;
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
 * Test tích hợp: kiểm tra hệ thống thành tích (Achievements) chạy thật trên MySQL.
 *
 * <p>Kịch bản đã verify bằng tay qua curl:
 * <ul>
 *   <li>Mới đăng ký: 16 achievement seed, 4 achievement secret (STREAK_365, COMEBACK,
 *       EARLY_BIRD, NIGHT_OWL) bị ẩn → chỉ hiện 12.</li>
 *   <li>Hoàn 1 bài học → FIRST_LESSON unlock (progress=1).</li>
 * </ul>
 *
 * <p>{@code @Transactional} ở class giúp mọi thay đổi (user/topic/lesson/achievement test
 * tạo ra) tự động rollback sau mỗi test, không làm bẩn dữ liệu dev thật trong DB.</p>
 */
@AutoConfigureMockMvc
@org.springframework.boot.test.context.SpringBootTest
@Transactional
class AchievementUnlockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String bearerToken;
    private Long lessonId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("ach-" + UUID.randomUUID() + "@test.com")
                .role("LEARNER")
                .displayName("Achievement User")
                .build());
        bearerToken = "Bearer " + jwtTokenProvider.generateToken(user);

        Topic topic = topicRepository.save(Topic.builder().title("ACH Topic").orderIndex(1).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .topicId(topic.getId())
                .title("ACH Lesson")
                .orderIndex(1)
                .lessonType(LessonType.NORMAL)
                .build());
        lessonId = lesson.getId();
    }

    /**
     * Khi user chưa làm gì: 4 achievement secret phải bị ẩn khỏi danh sách.
     * 16 seed - 4 secret = 12 achievement hiện ra.
     */
    @Test
    void listAchievements_beforeAnyActivity_hidesSecretAchievements() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/achievements")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'FIRST_LESSON')].unlocked").value(false))
                .andExpect(jsonPath("$[?(@.code == 'STREAK_365')]").isEmpty())
                .andExpect(jsonPath("$[?(@.code == 'COMEBACK')]").isEmpty())
                .andExpect(jsonPath("$[?(@.code == 'EARLY_BIRD')]").isEmpty())
                .andExpect(jsonPath("$[?(@.code == 'NIGHT_OWL')]").isEmpty())
                .andExpect(jsonPath("$.length()").value(12)); // 16 seed - 4 secret = 12
    }

    /**
     * Submit 1 bài hoàn hảo → FIRST_LESSON achievement unlock (progress=1).
     * Sau khi unlock, secret achievement (STREAK_365, COMEBACK) vẫn bị ẩn cho tới khi
     * user đạt điều kiện riêng (streak 365, reset streak >= 7).
     */
    @Test
    void submitLesson_unlocksFirstLessonAchievement() throws Exception {
        // Ban đầu: FIRST_LESSON chưa unlock, secret achievements bị ẩn.
        mockMvc.perform(get("/api/v1/users/me/achievements")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'FIRST_LESSON')].unlocked").value(false))
                .andExpect(jsonPath("$.length()").value(12)); // 16 seed - 4 secret = 12

        // Submit 1 bài hoàn hảo (0 sai).
        mockMvc.perform(post("/api/v1/lessons/" + lessonId + "/submit")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalQuestions\":1,\"totalCorrect\":1,\"totalMistakes\":0}"))
                .andExpect(status().isOk());

        // Sau khi submit: FIRST_LESSON unlock, progress = 1.
        mockMvc.perform(get("/api/v1/users/me/achievements")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'FIRST_LESSON')].unlocked").value(true))
                .andExpect(jsonPath("$[?(@.code == 'FIRST_LESSON')].progress").value(1))
                // Secret achievement chưa unlock vẫn bị ẩn (không có event nào hit threshold).
                .andExpect(jsonPath("$[?(@.code == 'STREAK_365')]").isEmpty())
                .andExpect(jsonPath("$[?(@.code == 'COMEBACK')]").isEmpty());
    }
}