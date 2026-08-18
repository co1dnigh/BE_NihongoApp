package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.config.MistakeReviewProperties;
import com.example.nihongo_app.dto.request.AnswerItem;
import com.example.nihongo_app.dto.request.ReviewSubmitRequest;
import com.example.nihongo_app.dto.response.MistakeSummaryResponse;
import com.example.nihongo_app.dto.response.ReviewSessionResponse;
import com.example.nihongo_app.dto.response.ReviewSubmitResponse;
import com.example.nihongo_app.entity.LessonAttemptAnswer;
import com.example.nihongo_app.entity.LessonQuestionOption;
import com.example.nihongo_app.entity.Mistake;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.LessonQuestionOptionRepository;
import com.example.nihongo_app.repository.LessonQuestionRepository;
import com.example.nihongo_app.repository.MistakeRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.EnergyService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test cho {@link MistakeServiceImpl}: upsert Mistake Bank tu bai hoc thuong (Phan 2) va
 * cham diem + luat "xoa no" cua phien on tap (Phan 3).
 */
@ExtendWith(MockitoExtension.class)
class MistakeServiceImplTest {

    @Mock private MistakeRepository mistakeRepository;
    @Mock private LessonQuestionRepository questionRepository;
    @Mock private LessonQuestionOptionRepository optionRepository;
    @Mock private UserRepository userRepository;
    @Mock private EnergyService energyService;
    @Spy private MistakeReviewProperties properties = new MistakeReviewProperties();

    @InjectMocks
    private MistakeServiceImpl service;

    private static final Long USER_ID = 1L;
    private static final Long QUESTION_ID = 100L;
    private static final Long OPTION_ID = 200L;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(USER_ID).currentEnergy(10).maxEnergy(25).build();
    }

    private LessonQuestionOption option(Long id, Long questionId, boolean correct) {
        return LessonQuestionOption.builder().id(id).questionId(questionId).correct(correct).build();
    }

    private LessonAttemptAnswer gradedAnswer(Long questionId, boolean correct) {
        return LessonAttemptAnswer.builder()
                .userId(USER_ID).lessonId(10L).questionId(questionId)
                .selectedOptionId(OPTION_ID).isCorrect(correct).answeredAt(LocalDateTime.now())
                .build();
    }

    private AnswerItem answerItem(Long questionId, Long selectedOptionId) {
        AnswerItem item = new AnswerItem();
        item.setQuestionId(questionId);
        item.setSelectedOptionId(selectedOptionId);
        return item;
    }

    // ============================ recordFromAnswers (Phan 2) ============================

    @Test
    void recordFromAnswers_firstWrongAnswer_createsNewActiveMistake() {
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.empty());

        service.recordFromAnswers(USER_ID, List.of(gradedAnswer(QUESTION_ID, false)));

        ArgumentCaptor<Mistake> captor = ArgumentCaptor.forClass(Mistake.class);
        verify(mistakeRepository).save(captor.capture());
        Mistake saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getQuestionId()).isEqualTo(QUESTION_ID);
        assertThat(saved.getWrongCount()).isEqualTo(1);
        assertThat(saved.getCorrectStreak()).isZero();
        assertThat(saved.getStatus()).isEqualTo(Mistake.Status.ACTIVE);
    }

    @Test
    void recordFromAnswers_correctAnswer_doesNotTouchMistakeBank() {
        service.recordFromAnswers(USER_ID, List.of(gradedAnswer(QUESTION_ID, true)));

        verify(mistakeRepository, never()).findByUserIdAndQuestionId(any(), any());
        verify(mistakeRepository, never()).save(any());
    }

    @Test
    void recordFromAnswers_wrongAgainOnActiveMistake_incrementsWrongCountAndResetsStreak() {
        Mistake existing = Mistake.builder()
                .id(1L).userId(USER_ID).questionId(QUESTION_ID)
                .wrongCount(2).correctStreak(1).status(Mistake.Status.ACTIVE)
                .lastWrongAt(LocalDateTime.now().minusDays(3))
                .build();
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.of(existing));

        service.recordFromAnswers(USER_ID, List.of(gradedAnswer(QUESTION_ID, false)));

        assertThat(existing.getWrongCount()).isEqualTo(3);
        assertThat(existing.getCorrectStreak()).isZero(); // reset
        assertThat(existing.getStatus()).isEqualTo(Mistake.Status.ACTIVE);
        verify(mistakeRepository).save(existing);
    }

    @Test
    void recordFromAnswers_wrongOnResolvedMistake_reopensToActive() {
        Mistake resolved = Mistake.builder()
                .id(1L).userId(USER_ID).questionId(QUESTION_ID)
                .wrongCount(1).correctStreak(2).status(Mistake.Status.RESOLVED)
                .lastCorrectAt(LocalDateTime.now().minusDays(5))
                .build();
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.of(resolved));

        service.recordFromAnswers(USER_ID, List.of(gradedAnswer(QUESTION_ID, false)));

        assertThat(resolved.getStatus()).isEqualTo(Mistake.Status.ACTIVE); // mo lai
        assertThat(resolved.getWrongCount()).isEqualTo(2);
        assertThat(resolved.getCorrectStreak()).isZero();
    }

    @Test
    void recordFromAnswers_emptyList_doesNothing() {
        service.recordFromAnswers(USER_ID, List.of());

        verify(mistakeRepository, never()).save(any());
    }

    // ============================ submitReviewSession: cau SAI ============================

    @Test
    void submitReviewSession_wrongAnswer_incrementsWrongCountResetsStreakStaysActive() {
        Mistake mistake = Mistake.builder()
                .id(1L).userId(USER_ID).questionId(QUESTION_ID)
                .wrongCount(1).correctStreak(1).status(Mistake.Status.ACTIVE)
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.of(mistake));
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, false)));
        when(optionRepository.findFirstByQuestionIdAndCorrectTrue(QUESTION_ID))
                .thenReturn(Optional.of(option(999L, QUESTION_ID, true)));

        ReviewSubmitRequest req = new ReviewSubmitRequest();
        req.setAnswers(List.of(answerItem(QUESTION_ID, OPTION_ID)));

        ReviewSubmitResponse response = service.submitReviewSession(USER_ID, req);

        assertThat(mistake.getWrongCount()).isEqualTo(2);
        assertThat(mistake.getCorrectStreak()).isZero();
        assertThat(mistake.getStatus()).isEqualTo(Mistake.Status.ACTIVE);
        assertThat(response.getResults().get(0).getCorrect()).isFalse();
        assertThat(response.getResults().get(0).getCorrectOptionId()).isEqualTo(999L);
        assertThat(response.getResolvedCount()).isZero();
    }

    // ============================ submitReviewSession: luat "xoa no" ============================

    @Test
    void submitReviewSession_firstCorrectAnswer_streakBecomes1_notResolvedYet() {
        Mistake mistake = Mistake.builder()
                .id(1L).userId(USER_ID).questionId(QUESTION_ID)
                .wrongCount(1).correctStreak(0).status(Mistake.Status.ACTIVE)
                .lastCorrectAt(null) // chua tung dung lan nao
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.of(mistake));
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, true)));
        when(optionRepository.findFirstByQuestionIdAndCorrectTrue(QUESTION_ID))
                .thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, true)));

        ReviewSubmitRequest req = new ReviewSubmitRequest();
        req.setAnswers(List.of(answerItem(QUESTION_ID, OPTION_ID)));

        ReviewSubmitResponse response = service.submitReviewSession(USER_ID, req);

        assertThat(mistake.getCorrectStreak()).isEqualTo(1);
        assertThat(mistake.getStatus()).isEqualTo(Mistake.Status.ACTIVE); // chua du 2 lan
        assertThat(response.getResolvedCount()).isZero();
    }

    @Test
    void submitReviewSession_secondCorrectSameSession_notResolved_gapTooSmall() {
        // Lan dung truoc chi moi 1 tieng truoc (< 1 ngay theo resolve-min-gap-days mac dinh).
        Mistake mistake = Mistake.builder()
                .id(1L).userId(USER_ID).questionId(QUESTION_ID)
                .wrongCount(1).correctStreak(1).status(Mistake.Status.ACTIVE)
                .lastCorrectAt(LocalDateTime.now().minusHours(1))
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.of(mistake));
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, true)));
        when(optionRepository.findFirstByQuestionIdAndCorrectTrue(QUESTION_ID))
                .thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, true)));

        ReviewSubmitRequest req = new ReviewSubmitRequest();
        req.setAnswers(List.of(answerItem(QUESTION_ID, OPTION_ID)));

        ReviewSubmitResponse response = service.submitReviewSession(USER_ID, req);

        assertThat(mistake.getCorrectStreak()).isEqualTo(2); // streak van tang
        assertThat(mistake.getStatus()).isEqualTo(Mistake.Status.ACTIVE); // nhung chua "xoa no"
        assertThat(response.getResolvedCount()).isZero();
    }

    @Test
    void submitReviewSession_secondCorrectAfterOneDay_resolvesMistake() {
        LocalDateTime yesterday = LocalDateTime.of(2026, 8, 1, 10, 0);
        LocalDateTime today = LocalDateTime.of(2026, 8, 2, 10, 0);

        Mistake mistake = Mistake.builder()
                .id(1L).userId(USER_ID).questionId(QUESTION_ID)
                .wrongCount(1).correctStreak(1).status(Mistake.Status.ACTIVE)
                .lastCorrectAt(yesterday)
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.of(mistake));
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, true)));
        when(optionRepository.findFirstByQuestionIdAndCorrectTrue(QUESTION_ID))
                .thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, true)));

        ReviewSubmitRequest req = new ReviewSubmitRequest();
        req.setAnswers(List.of(answerItem(QUESTION_ID, OPTION_ID)));

        ReviewSubmitResponse response;
        try (MockedStatic<LocalDateTime> mockedTime =
                     Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(today);
            response = service.submitReviewSession(USER_ID, req);
        }

        assertThat(mistake.getCorrectStreak()).isEqualTo(2);
        assertThat(mistake.getStatus()).isEqualTo(Mistake.Status.RESOLVED);
        assertThat(response.getResolvedCount()).isEqualTo(1);
        assertThat(response.getResults().get(0).getResolved()).isTrue();
    }

    @Test
    void submitReviewSession_answerForUnknownMistake_isSkipped() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, true)));
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.empty());

        ReviewSubmitRequest req = new ReviewSubmitRequest();
        req.setAnswers(List.of(answerItem(QUESTION_ID, OPTION_ID)));

        ReviewSubmitResponse response = service.submitReviewSession(USER_ID, req);

        assertThat(response.getResults()).isEmpty();
        verify(mistakeRepository, never()).save(any());
        // Khong co answer hop le nao -> khong thuong nang luong.
        assertThat(response.getEnergyRewarded()).isZero();
        verify(energyService, never()).addEnergy(any(), anyInt());
    }

    @Test
    void submitReviewSession_optionNotBelongingToQuestion_isSkipped() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        // Option 200 thuc te thuoc question 999, khong phai QUESTION_ID nhu FE khai bao.
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(option(OPTION_ID, 999L, true)));

        ReviewSubmitRequest req = new ReviewSubmitRequest();
        req.setAnswers(List.of(answerItem(QUESTION_ID, OPTION_ID)));

        ReviewSubmitResponse response = service.submitReviewSession(USER_ID, req);

        assertThat(response.getResults()).isEmpty();
        verify(mistakeRepository, never()).findByUserIdAndQuestionId(any(), any());
    }

    // ============================ thuong nang luong (3.2) ============================

    @Test
    void submitReviewSession_validSubmit_rewardsEnergyOnce() {
        Mistake mistake = Mistake.builder()
                .id(1L).userId(USER_ID).questionId(QUESTION_ID)
                .wrongCount(1).correctStreak(0).status(Mistake.Status.ACTIVE)
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.of(mistake));
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, false)));
        when(optionRepository.findFirstByQuestionIdAndCorrectTrue(QUESTION_ID))
                .thenReturn(Optional.of(option(999L, QUESTION_ID, true)));

        ReviewSubmitRequest req = new ReviewSubmitRequest();
        req.setAnswers(List.of(answerItem(QUESTION_ID, OPTION_ID)));

        ReviewSubmitResponse response = service.submitReviewSession(USER_ID, req);

        assertThat(response.getEnergyRewarded()).isEqualTo(5); // energy-reward mac dinh
        assertThat(user.getMistakeReviewRewardCountToday()).isEqualTo(1);
        verify(energyService).addEnergy(USER_ID, 5);
    }

    @Test
    void submitReviewSession_alreadyUsedAllRewardsToday_noMoreEnergyButStillGrades() {
        user.setLastMistakeReviewRewardDate(LocalDate.now());
        user.setMistakeReviewRewardCountToday(2); // da dung het 2 luot mac dinh trong ngay

        Mistake mistake = Mistake.builder()
                .id(1L).userId(USER_ID).questionId(QUESTION_ID)
                .wrongCount(1).correctStreak(0).status(Mistake.Status.ACTIVE)
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(mistakeRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID)).thenReturn(Optional.of(mistake));
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(option(OPTION_ID, QUESTION_ID, false)));
        when(optionRepository.findFirstByQuestionIdAndCorrectTrue(QUESTION_ID))
                .thenReturn(Optional.of(option(999L, QUESTION_ID, true)));

        ReviewSubmitRequest req = new ReviewSubmitRequest();
        req.setAnswers(List.of(answerItem(QUESTION_ID, OPTION_ID)));

        ReviewSubmitResponse response = service.submitReviewSession(USER_ID, req);

        assertThat(response.getEnergyRewarded()).isZero(); // het luot thuong...
        assertThat(mistake.getWrongCount()).isEqualTo(2); // ...nhung van cham diem binh thuong
        verify(energyService, never()).addEnergy(any(), anyInt());
    }

    // ============================ getSummary / startReviewSession ============================

    @Test
    void getSummary_returnsActiveCountAndRemainingRewardedSessions() {
        when(mistakeRepository.countByUserIdAndStatus(USER_ID, Mistake.Status.ACTIVE)).thenReturn(7L);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user)); // chua dung luot nao hom nay

        MistakeSummaryResponse summary = service.getSummary(USER_ID);

        assertThat(summary.getActiveCount()).isEqualTo(7L);
        assertThat(summary.getReviewableToday()).isEqualTo(2); // rewarded-sessions-per-day mac dinh
    }

    @Test
    void startReviewSession_noActiveMistakes_returnsEmptyWithMessage() {
        when(mistakeRepository.findAllByUserIdAndStatusOrderByWrongCountDescLastWrongAtDesc(
                any(), any(), any())).thenReturn(List.of());

        ReviewSessionResponse response = service.startReviewSession(USER_ID);

        assertThat(response.getQuestions()).isEmpty();
        assertThat(response.getMessage()).isNotBlank();
    }
}
