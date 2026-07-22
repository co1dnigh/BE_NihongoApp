package com.example.nihongo_app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nihongo_app.config.SecurityConfig;
import com.example.nihongo_app.dto.request.SubmitAnswerRequest;
import com.example.nihongo_app.dto.response.SubmitAnswerResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.JwtTokenProvider;
import com.example.nihongo_app.service.ExerciseService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExerciseController.class)
@Import(SecurityConfig.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseService exerciseService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void submitAnswer_withoutToken_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/exercises/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Konnichiwa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitAnswer_withValidToken_returnsGradingResult() throws Exception {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(anyString())).thenReturn("learner@test.com");
        when(userRepository.findByEmail("learner@test.com")).thenReturn(
                Optional.of(User.builder().id(1L).email("learner@test.com").role("LEARNER").build()));
        when(exerciseService.submitAnswer(eq("learner@test.com"), eq(1L), any(SubmitAnswerRequest.class)))
                .thenReturn(SubmitAnswerResponse.builder().correct(true).correctAnswer("Konnichiwa").build());

        mockMvc.perform(post("/api/v1/exercises/1/submit")
                        .header("Authorization", "Bearer faketoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"konnichiwa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.correctAnswer").value("Konnichiwa"));
    }
}
