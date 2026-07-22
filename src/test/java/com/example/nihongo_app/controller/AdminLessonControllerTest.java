package com.example.nihongo_app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nihongo_app.config.SecurityConfig;
import com.example.nihongo_app.dto.request.LessonRequest;
import com.example.nihongo_app.dto.response.LessonAdminResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.LessonService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminLessonController.class)
@Import(SecurityConfig.class)
class AdminLessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonService lessonService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserRepository userRepository;

    private void mockAuthenticatedUser(String email, String role) {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(User.builder().id(1L).email(email).role(role).build()));
    }

    @Test
    void createLesson_asLearner_returns403() throws Exception {
        mockAuthenticatedUser("learner@test.com", "LEARNER");

        mockMvc.perform(post("/api/v1/admin/lessons")
                        .header("Authorization", "Bearer faketoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jlptLevel\":\"N5\",\"title\":\"Bai 1\",\"orderIndex\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createLesson_asAdmin_returns201() throws Exception {
        mockAuthenticatedUser("admin@test.com", "ADMIN");
        when(lessonService.createLesson(any(LessonRequest.class))).thenReturn(
                LessonAdminResponse.builder().id(1L).jlptLevel("N5").title("Bai 1").orderIndex(1).build());

        mockMvc.perform(post("/api/v1/admin/lessons")
                        .header("Authorization", "Bearer faketoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jlptLevel\":\"N5\",\"title\":\"Bai 1\",\"orderIndex\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createLesson_asAdmin_invalidBody_returns400() throws Exception {
        mockAuthenticatedUser("admin@test.com", "ADMIN");

        mockMvc.perform(post("/api/v1/admin/lessons")
                        .header("Authorization", "Bearer faketoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bai 1\",\"orderIndex\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLesson_withoutToken_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jlptLevel\":\"N5\",\"title\":\"Bai 1\",\"orderIndex\":1}"))
                .andExpect(status().isForbidden());
    }
}
