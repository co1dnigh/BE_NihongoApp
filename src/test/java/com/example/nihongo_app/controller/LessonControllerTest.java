package com.example.nihongo_app.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nihongo_app.config.SecurityConfig;
import com.example.nihongo_app.dto.response.LessonDetailResponse;
import com.example.nihongo_app.dto.response.LessonSummaryResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.LessonService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(LessonController.class)
@Import(SecurityConfig.class)
class LessonControllerTest {

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
    void getLessonsByLevel_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/lessons").param("level", "N5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLessonsByLevel_withValidToken_returns200() throws Exception {
        mockAuthenticatedUser("learner@test.com", "LEARNER");
        when(lessonService.getLessonsByLevel("learner@test.com", "N5")).thenReturn(List.of(
                LessonSummaryResponse.builder().id(1L).jlptLevel("N5").title("Bai 1").orderIndex(1)
                        .status("IN_PROGRESS").build()));

        mockMvc.perform(get("/api/v1/lessons").param("level", "N5")
                        .header("Authorization", "Bearer faketoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    void getLessonDetail_whenLocked_returns403FromService() throws Exception {
        mockAuthenticatedUser("learner@test.com", "LEARNER");
        when(lessonService.getLessonDetail("learner@test.com", 2L))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Lesson is locked"));

        mockMvc.perform(get("/api/v1/lessons/2").header("Authorization", "Bearer faketoken"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLessonDetail_whenUnlocked_returns200WithoutCorrectAnswer() throws Exception {
        mockAuthenticatedUser("learner@test.com", "LEARNER");
        when(lessonService.getLessonDetail("learner@test.com", 1L)).thenReturn(
                LessonDetailResponse.builder().id(1L).jlptLevel("N5").title("Bai 1").orderIndex(1)
                        .status("IN_PROGRESS").vocabularies(List.of()).exercises(List.of()).build());

        mockMvc.perform(get("/api/v1/lessons/1").header("Authorization", "Bearer faketoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void completeLesson_returns204() throws Exception {
        mockAuthenticatedUser("learner@test.com", "LEARNER");

        mockMvc.perform(post("/api/v1/lessons/1/complete").header("Authorization", "Bearer faketoken"))
                .andExpect(status().isNoContent());
    }
}
