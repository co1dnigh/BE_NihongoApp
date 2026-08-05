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
 * Test tích hợp chạy thật với MySQL docker local (không dùng H2/Testcontainers vì
 * project chưa có sẵn), mô phỏng lại đúng kịch bản đã verify bằng tay qua curl:
 * hoàn thành Daily Quest -> mở Rương thưởng -> mua Streak Freeze bằng coin.
 *
 * <p>{@code @Transactional} ở class giúp mọi thay đổi (user/topic/lesson/quest test
 * tạo ra) tự động rollback sau mỗi test, không làm bẩn dữ liệu dev thật trong DB.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CoinQuestChestIntegrationTest {

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
    private Long testUserId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("it-" + UUID.randomUUID() + "@test.com")
                .role("LEARNER")
                .displayName("Integration Test User")
                .build());
        testUserId = user.getId();
        bearerToken = "Bearer " + jwtTokenProvider.generateToken(user);

        Topic topic = topicRepository.save(Topic.builder().title("IT Topic").orderIndex(1).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .topicId(topic.getId())
                .title("IT Lesson")
                .orderIndex(1)
                .lessonType(LessonType.NORMAL)
                .build());
        lessonId = lesson.getId();
    }

    @Test
    void fullFlow_earnCoins_completeDailyQuests_openChest_buyStreakFreeze() throws Exception {
        // 1. Lan dau goi /quests -> tu dong tao 3 quest ngau nhien.
        mockMvc.perform(get("/api/v1/users/me/quests").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // 2. Ruong chua san sang (chua lam quest nao).
        mockMvc.perform(get("/api/v1/users/me/chest").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        // 3. Nop bai 2 lan, moi lan hoan hao (0 sai) + 20 cau dung -> vuot moc cao nhat
        //    co the co cua ca 3 loai quest hien tai (COMPLETE_LESSONS<=2, CORRECT_ANSWERS<=20,
        //    PERFECT_LESSON=1), bat ke he thong random gan dung 3 quest nao trong 5 dinh nghia goc.
        String perfectSubmitBody = "{\"totalQuestions\":20,\"totalCorrect\":20,\"totalMistakes\":0}";
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/lessons/" + lessonId + "/submit")
                            .header("Authorization", bearerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(perfectSubmitBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.coinsEarned").value(12)); // NORMAL base 8 + perfect bonus 4
        }

        // 4. Tat ca quest hom nay phai da hoan thanh -> ruong san sang.
        mockMvc.perform(get("/api/v1/users/me/quests").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completed").value(true))
                .andExpect(jsonPath("$[1].completed").value(true))
                .andExpect(jsonPath("$[2].completed").value(true));

        mockMvc.perform(get("/api/v1/users/me/chest").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        // 5. Mo ruong lan 1 -> thanh cong, nhan coin ngau nhien trong [30, 100].
        mockMvc.perform(post("/api/v1/users/me/chest/open").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinsRewarded").isNumber());

        // 6. Mo lai lan 2 trong cung ngay -> bi chan.
        mockMvc.perform(post("/api/v1/users/me/chest/open").header("Authorization", bearerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ban da mo ruong hom nay roi, quay lai vao ngay mai."));

        // 7. Mua Streak Freeze: coin hien co (24 tu bai hoc + 30-100 tu ruong) nhieu kha nang
        //    van chua du 200 -> kiem tra tu choi dung cach.
        User afterChest = userRepository.findById(testUserId).orElseThrow();
        if (afterChest.getCoins() < 200) {
            mockMvc.perform(post("/api/v1/users/me/streak/freeze/buy").header("Authorization", bearerToken))
                    .andExpect(status().isBadRequest());
        }

        // 8. Nap du 300 coin truc tiep (mo phong da tich luy) roi mua lai -> thanh cong.
        User topUp = userRepository.findById(testUserId).orElseThrow();
        topUp.setCoins(300);
        userRepository.save(topUp);

        mockMvc.perform(post("/api/v1/users/me/streak/freeze/buy").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streakFreezeCount").value(1));
    }
}
