package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.request.SubmitLessonRequest;
import com.example.nihongo_app.dto.response.CancelLessonResponse;
import com.example.nihongo_app.dto.response.RoadmapLessonResponse.Status;
import com.example.nihongo_app.dto.response.StartLessonResponse;
import com.example.nihongo_app.dto.response.SubmitLessonResponse;
import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.CoinTransaction.TransactionType;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.LessonQuestion;
import com.example.nihongo_app.entity.LessonQuestion.QuestionType;
import com.example.nihongo_app.entity.QuestDefinition.QuestType;
import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.UserLessonProgress.ProgressStatus;
import com.example.nihongo_app.exception.InsufficientEnergyException;
import com.example.nihongo_app.exception.LessonLockedException;
import com.example.nihongo_app.repository.CoinTransactionRepository;
import com.example.nihongo_app.repository.LessonQuestionOptionRepository;
import com.example.nihongo_app.repository.LessonQuestionRepository;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.UserExpLogRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.DailyQuestService;
import com.example.nihongo_app.service.EnergyService;
import com.example.nihongo_app.service.LessonUnlockPolicy;
import com.example.nihongo_app.service.ShopService;
import com.example.nihongo_app.service.StreakService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Test cho {@link LessonAttemptServiceImpl}: vòng đời start/submit/cancel.
 *
 * <p>Nhóm test coin/Daily Quest/streak-milestone (bổ sung cho tính năng rương thưởng)
 * nằm ở phần cuối file; nhóm start/cancel test lại các nhánh cốt lõi vốn có từ trước
 * (unlock, trừ/hoàn năng lượng, chọn đề thi).</p>
 */
@ExtendWith(MockitoExtension.class)
class LessonAttemptServiceImplTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private LessonQuestionRepository questionRepository;
    @Mock private LessonQuestionOptionRepository optionRepository;
    @Mock private UserLessonProgressRepository progressRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserExpLogRepository expLogRepository;
    @Mock private LessonUnlockPolicy unlockPolicy;
    @Mock private StreakService streakService;
    @Mock private EnergyService energyService;
    @Mock private CoinTransactionRepository coinTransactionRepository;
    @Mock private DailyQuestService dailyQuestService;
    @Mock private ShopService shopService;

    @InjectMocks
    private LessonAttemptServiceImpl service;

    private static final Long USER_ID = 1L;
    private static final Long LESSON_ID = 10L;
    private static final Long TOPIC_ID = 100L;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(USER_ID).coins(0).exp(0).build();
        // lenient(): mot so test (vd lessonLocked/insufficientEnergy) throw truoc khi dung
        // toi cac stub nay, Mockito strict-stubbing se bao "unnecessary" neu khong danh dau.
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        lenient().when(progressRepository.findByUserIdAndLessonId(eq(USER_ID), any())).thenReturn(Optional.empty());
    }

    private Lesson lessonOfType(LessonType type) {
        return Lesson.builder().id(LESSON_ID).topicId(TOPIC_ID).title("Test lesson")
                .orderIndex(1).lessonType(type).build();
    }

    private SubmitLessonRequest request(int totalQuestions, int totalCorrect, int totalMistakes) {
        SubmitLessonRequest req = new SubmitLessonRequest();
        req.setTotalQuestions(totalQuestions);
        req.setTotalCorrect(totalCorrect);
        req.setTotalMistakes(totalMistakes);
        return req;
    }

    private JsonNode configJson(String json) {
        return new ObjectMapper().readTree(json);
    }

    private Lesson lessonWithConfig(LessonType type, String configJson) {
        return Lesson.builder().id(LESSON_ID).topicId(TOPIC_ID).title("Test lesson")
                .orderIndex(1).lessonType(type).configJson(configJson(configJson)).build();
    }

    @Test
    void submitNormalLesson_perfect_awardsBaseAndPerfectBonusCoins() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));

        SubmitLessonResponse response = service.submitLesson(LESSON_ID, USER_ID, request(10, 10, 0));

        assertThat(response.getCoinsEarned()).isEqualTo(12); // base 8 + perfect bonus 4

        ArgumentCaptor<CoinTransaction> captor = ArgumentCaptor.forClass(CoinTransaction.class);
        verify(coinTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(12);
        assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.EARN_LESSON);
        assertThat(captor.getValue().getReferenceId()).isEqualTo(LESSON_ID);

        verify(dailyQuestService).recordProgress(USER_ID, QuestType.COMPLETE_LESSONS, 1);
        verify(dailyQuestService).recordProgress(USER_ID, QuestType.CORRECT_ANSWERS, 10);
        verify(dailyQuestService).recordProgress(USER_ID, QuestType.PERFECT_LESSON, 1);
    }

    @Test
    void submitNormalLesson_withMistakes_noPerfectBonusOrQuest() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));

        SubmitLessonResponse response = service.submitLesson(LESSON_ID, USER_ID, request(10, 8, 2));

        assertThat(response.getCoinsEarned()).isEqualTo(8); // base only, no perfect bonus

        verify(dailyQuestService).recordProgress(USER_ID, QuestType.CORRECT_ANSWERS, 8);
        verify(dailyQuestService, never()).recordProgress(eq(USER_ID), eq(QuestType.PERFECT_LESSON), anyInt());
    }

    @Test
    void submitTimedReview_perfect_awardsBaseAndPerfectBonusCoins() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.TIMED_REVIEW)));

        SubmitLessonRequest req = request(10, 10, 0);
        SubmitLessonResponse response = service.submitLesson(LESSON_ID, USER_ID, req);

        assertThat(response.getCoinsEarned()).isEqualTo(9); // base 6 + perfect bonus 3
    }

    @Test
    void submitJumpTest_pass_awardsFlatCoins() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.JUMP_TEST)));
        SubmitLessonRequest req = request(10, 10, 0);
        req.setHeartsRemaining(2);

        SubmitLessonResponse response = service.submitLesson(LESSON_ID, USER_ID, req);

        assertThat(response.getCoinsEarned()).isEqualTo(20);
    }

    @Test
    void submitJumpTest_fail_noCoinsAndNoQuestProgress() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.JUMP_TEST)));
        SubmitLessonRequest req = request(10, 3, 7);
        req.setHeartsRemaining(0);

        SubmitLessonResponse response = service.submitLesson(LESSON_ID, USER_ID, req);

        assertThat(response.getCoinsEarned()).isZero();
        verify(coinTransactionRepository, never()).save(any());
        verify(dailyQuestService, never()).recordProgress(any(), any(), anyInt());
    }

    @Test
    void submitLesson_replay_reducesCoinsByDefaultRatio() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        SubmitLessonRequest req = request(10, 10, 0);
        req.setIsReplay(true);

        SubmitLessonResponse response = service.submitLesson(LESSON_ID, USER_ID, req);

        // base+bonus 12 coin * 0.3 (ti le replay mac dinh) = 3.6 -> lam tron 4
        assertThat(response.getCoinsEarned()).isEqualTo(4);
    }

    @Test
    void submitLesson_streakReachesMilestone7_awardsCoinBonus() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        doAnswer(invocation -> {
            user.setCurrentStreak(7);
            return null;
        }).when(streakService).checkAndUpdateStreak(USER_ID);

        service.submitLesson(LESSON_ID, USER_ID, request(10, 10, 0));

        ArgumentCaptor<CoinTransaction> captor = ArgumentCaptor.forClass(CoinTransaction.class);
        verify(coinTransactionRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(tx -> tx.getTransactionType() == TransactionType.STREAK_BONUS && tx.getAmount() == 50);
    }

    @Test
    void submitLesson_streakNotAtMilestone_noCoinBonus() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        doAnswer(invocation -> {
            user.setCurrentStreak(8);
            return null;
        }).when(streakService).checkAndUpdateStreak(USER_ID);

        service.submitLesson(LESSON_ID, USER_ID, request(10, 10, 0));

        ArgumentCaptor<CoinTransaction> captor = ArgumentCaptor.forClass(CoinTransaction.class);
        verify(coinTransactionRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.EARN_LESSON);
    }

    // ============================ ENERGY REFUND (submit) ============================

    @Test
    void submitLesson_perfectLesson_refundsEnergyUpToPerfectAmount() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        user.setCurrentEnergy(0);
        user.setMaxEnergy(25);

        service.submitLesson(LESSON_ID, USER_ID, request(10, 10, 0)); // 0 sai -> hoan hao

        assertThat(user.getCurrentEnergy()).isEqualTo(5); // PERFECT_LESSON_ENERGY_REFUND
    }

    @Test
    void submitLesson_goodLesson_refundsSmallerAmount() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        user.setCurrentEnergy(0);
        user.setMaxEnergy(25);

        service.submitLesson(LESSON_ID, USER_ID, request(10, 8, 2)); // 2 loi -> "kha"

        assertThat(user.getCurrentEnergy()).isEqualTo(2); // GOOD_LESSON_ENERGY_REFUND
    }

    @Test
    void submitLesson_badLesson_noEnergyRefund() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        user.setCurrentEnergy(0);
        user.setMaxEnergy(25);

        service.submitLesson(LESSON_ID, USER_ID, request(10, 5, 5)); // 5 loi -> qua nguong "kha"

        assertThat(user.getCurrentEnergy()).isZero();
    }

    @Test
    void submitLesson_replayPerfectLesson_noEnergyRefund_preventsInfiniteFarming() {
        // Replay khong tru nang luong luc /start (mien phi), nen neu van hoan +5 luc /submit
        // se thanh farm nang luong vo han bang cach replay 1 bai da hoan thanh nhieu lan.
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        user.setCurrentEnergy(10);
        user.setMaxEnergy(25);
        SubmitLessonRequest req = request(10, 10, 0); // hoan hao
        req.setIsReplay(true);

        service.submitLesson(LESSON_ID, USER_ID, req);

        assertThat(user.getCurrentEnergy()).isEqualTo(10); // khong doi
    }

    @Test
    void submitLesson_cheapLesson_refundCappedAtActualEntryCost() {
        // Lesson co entryCostEnergy rieng (3) < muc hoan hao mac dinh (5)
        // -> khong duoc hoan nhieu hon so thuc su da tru luc /start.
        Lesson lesson = lessonWithConfig(LessonType.NORMAL, "{\"entryCostEnergy\":3}");
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        user.setCurrentEnergy(0);
        user.setMaxEnergy(25);

        service.submitLesson(LESSON_ID, USER_ID, request(10, 10, 0)); // hoan hao

        assertThat(user.getCurrentEnergy()).isEqualTo(3); // cap o entryCostEnergy=3, khong phai 5
    }

    // ============================ START ============================

    @Test
    void startLesson_lessonLocked_throwsLessonLockedException() {
        Lesson lesson = lessonOfType(LessonType.NORMAL);
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(unlockPolicy.evaluate(lesson, USER_ID)).thenReturn(Status.LOCKED);

        assertThatThrownBy(() -> service.startLesson(LESSON_ID, USER_ID))
                .isInstanceOf(LessonLockedException.class);
    }

    @Test
    void startLesson_firstAttempt_deductsDefaultEntryCost_andIsNotReplay() {
        Lesson lesson = lessonOfType(LessonType.NORMAL); // configJson=null -> default cost = 10
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(unlockPolicy.evaluate(lesson, USER_ID)).thenReturn(Status.UNLOCKED);
        when(questionRepository.findAllByLessonIdOrderByIdAsc(LESSON_ID)).thenReturn(List.of());
        user.setCurrentEnergy(10);

        StartLessonResponse response = service.startLesson(LESSON_ID, USER_ID);

        assertThat(response.getTotalEnergyDeducted()).isEqualTo(10);
        assertThat(response.getIsReplay()).isFalse();
        assertThat(user.getCurrentEnergy()).isZero();
        verify(userRepository).save(user);
    }

    @Test
    void startLesson_entryCostFromConfigJson_overridesDefault() {
        Lesson lesson = lessonWithConfig(LessonType.NORMAL, "{\"entryCostEnergy\":3}");
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(unlockPolicy.evaluate(lesson, USER_ID)).thenReturn(Status.UNLOCKED);
        when(questionRepository.findAllByLessonIdOrderByIdAsc(LESSON_ID)).thenReturn(List.of());
        user.setCurrentEnergy(10);

        StartLessonResponse response = service.startLesson(LESSON_ID, USER_ID);

        assertThat(response.getTotalEnergyDeducted()).isEqualTo(3);
        assertThat(user.getCurrentEnergy()).isEqualTo(7);
    }

    @Test
    void startLesson_insufficientEnergy_throwsInsufficientEnergyException() {
        Lesson lesson = lessonOfType(LessonType.NORMAL); // can 10 nang luong mac dinh
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(unlockPolicy.evaluate(lesson, USER_ID)).thenReturn(Status.UNLOCKED);
        user.setCurrentEnergy(8);

        assertThatThrownBy(() -> service.startLesson(LESSON_ID, USER_ID))
                .isInstanceOf(InsufficientEnergyException.class);

        assertThat(user.getCurrentEnergy()).isEqualTo(8); // khong bi tru khi that bai
        verify(userRepository, never()).save(any());
    }

    @Test
    void startLesson_replayCompletedLesson_freeEnergy() {
        Lesson lesson = lessonOfType(LessonType.NORMAL);
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(unlockPolicy.evaluate(lesson, USER_ID)).thenReturn(Status.COMPLETED);
        when(questionRepository.findAllByLessonIdOrderByIdAsc(LESSON_ID)).thenReturn(List.of());
        user.setCurrentEnergy(0); // du khong con nang luong, replay van phai duoc phep

        StartLessonResponse response = service.startLesson(LESSON_ID, USER_ID);

        assertThat(response.getIsReplay()).isTrue();
        assertThat(response.getTotalEnergyDeducted()).isZero();
        assertThat(user.getCurrentEnergy()).isZero(); // khong doi
        verify(userRepository, never()).save(any());
    }

    @Test
    void startLesson_picksLimitedQuestions_whenPoolLargerThanConfiguredSize() {
        Lesson lesson = lessonWithConfig(LessonType.NORMAL, "{\"questionsPerSession\":2}");
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(unlockPolicy.evaluate(lesson, USER_ID)).thenReturn(Status.UNLOCKED);
        user.setCurrentEnergy(10);
        List<LessonQuestion> pool = List.of(
                LessonQuestion.builder().id(1L).lessonId(LESSON_ID).questionType(QuestionType.TRANSLATE_TO_JP).build(),
                LessonQuestion.builder().id(2L).lessonId(LESSON_ID).questionType(QuestionType.TRANSLATE_TO_JP).build(),
                LessonQuestion.builder().id(3L).lessonId(LESSON_ID).questionType(QuestionType.TRANSLATE_TO_JP).build(),
                LessonQuestion.builder().id(4L).lessonId(LESSON_ID).questionType(QuestionType.TRANSLATE_TO_JP).build(),
                LessonQuestion.builder().id(5L).lessonId(LESSON_ID).questionType(QuestionType.TRANSLATE_TO_JP).build()
        );
        when(questionRepository.findAllByLessonIdOrderByIdAsc(LESSON_ID)).thenReturn(pool);
        when(optionRepository.findAllByQuestionIdOrderByOrderIndexAscIdAsc(any())).thenReturn(List.of());

        StartLessonResponse response = service.startLesson(LESSON_ID, USER_ID);

        assertThat(response.getQuestions()).hasSize(2); // gioi han theo questionsPerSession, khong phai ca 5 cau
    }

    // ============================ CANCEL ============================

    @Test
    void cancelLesson_progressInProgress_refundsEnergyCappedAtMax() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        user.setCurrentEnergy(15);
        user.setMaxEnergy(20);
        when(progressRepository.findByUserIdAndLessonId(USER_ID, LESSON_ID)).thenReturn(Optional.of(
                UserLessonProgress.builder().userId(USER_ID).lessonId(LESSON_ID)
                        .status(ProgressStatus.IN_PROGRESS).build()));

        CancelLessonResponse response = service.cancelLesson(LESSON_ID, USER_ID);

        // Hoan 10 (default entry cost) nhung 15+10=25 vuot max 20 -> cap lai con 20.
        assertThat(response.getEnergyRefunded()).isEqualTo(10);
        assertThat(response.getCurrentEnergy()).isEqualTo(20);
        assertThat(user.getCurrentEnergy()).isEqualTo(20);
    }

    @Test
    void cancelLesson_progressCompleted_noRefund() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        user.setCurrentEnergy(5);
        when(progressRepository.findByUserIdAndLessonId(USER_ID, LESSON_ID)).thenReturn(Optional.of(
                UserLessonProgress.builder().userId(USER_ID).lessonId(LESSON_ID)
                        .status(ProgressStatus.COMPLETED).build()));

        CancelLessonResponse response = service.cancelLesson(LESSON_ID, USER_ID);

        assertThat(response.getEnergyRefunded()).isZero();
        assertThat(user.getCurrentEnergy()).isEqualTo(5);
        verify(userRepository, never()).save(any());
    }

    @Test
    void cancelLesson_noExistingProgress_noRefund() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lessonOfType(LessonType.NORMAL)));
        user.setCurrentEnergy(5);
        // @BeforeEach da stub findByUserIdAndLessonId tra ve Optional.empty() mac dinh.

        CancelLessonResponse response = service.cancelLesson(LESSON_ID, USER_ID);

        assertThat(response.getEnergyRefunded()).isZero();
        assertThat(response.getCurrentEnergy()).isEqualTo(5);
    }
}
