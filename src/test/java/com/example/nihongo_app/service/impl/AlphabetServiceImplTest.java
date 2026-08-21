package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.request.SubmitAlphabetPracticeRequest;
import com.example.nihongo_app.dto.response.AlphabetPracticeResponse;
import com.example.nihongo_app.dto.response.SubmitAlphabetPracticeResponse;
import com.example.nihongo_app.entity.Character;
import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserCharacterProgress;
import com.example.nihongo_app.repository.CharacterRepository;
import com.example.nihongo_app.repository.RankRepository;
import com.example.nihongo_app.repository.UserCharacterProgressRepository;
import com.example.nihongo_app.repository.UserExpLogRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlphabetServiceImplTest {

    @Mock private CharacterRepository characterRepository;
    @Mock private UserCharacterProgressRepository progressRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserExpLogRepository expLogRepository;
    @Mock private RankRepository rankRepository;

    @InjectMocks
    private AlphabetServiceImpl service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).exp(995).rank(Rank.builder().id(1L).name("BRONZE").build()).build();
    }

    @Test
    void startPractice_returnsTenQuestionsWithFourOptionsForMultipleChoice() {
        List<Character> characters = java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(id -> Character.builder().id((long) id).symbol("あ" + id).romaji("a" + id)
                        .type(Character.CharacterType.HIRAGANA).groupName("A").orderIndex(id)
                        .build())
                .toList();
        when(characterRepository.findAll()).thenReturn(characters);
        when(progressRepository.findAllByUserId(1L)).thenReturn(List.of());

        AlphabetPracticeResponse response = service.startPractice(1L);

        assertThat(response.getQuestions()).hasSize(10);
        response.getQuestions().stream()
                .filter(question -> "MULTIPLE_CHOICE".equals(question.getQuestionType()))
                .forEach(question -> assertThat(question.getOptions()).hasSize(4));
    }

    @Test
    void submitPractice_clampsMasteryAndAwardsExpWithRankPromotion() {
        Character character = Character.builder().id(1L).symbol("あ").build();
        UserCharacterProgress progress = UserCharacterProgress.builder()
                .id(5L).user(user).character(character).masteryLevel(3).build();
        SubmitAlphabetPracticeRequest.Result result = new SubmitAlphabetPracticeRequest.Result();
        result.setCharacterId(1L);
        result.setIsCorrect(true);
        Rank silver = Rank.builder().id(2L).name("SILVER").minExpRequired(1000).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(progressRepository.findByUserIdAndCharacterId(1L, 1L)).thenReturn(Optional.of(progress));
        when(rankRepository.findFirstByMinExpRequiredLessThanEqualOrderByOrderIndexDesc(1000))
                .thenReturn(Optional.of(silver));

        SubmitAlphabetPracticeResponse response = service.submitPractice(1L,
                new SubmitAlphabetPracticeRequest(List.of(result)));

        assertThat(progress.getMasteryLevel()).isEqualTo(3);
        assertThat(response.getExpEarned()).isEqualTo(5);
        assertThat(response.getCurrentExp()).isEqualTo(1000);
        assertThat(response.isPromoted()).isTrue();
        assertThat(response.getNewRankName()).isEqualTo("SILVER");
        verify(expLogRepository).save(any());
    }
}