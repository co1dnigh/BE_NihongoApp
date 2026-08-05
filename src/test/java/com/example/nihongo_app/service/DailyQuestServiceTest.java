package com.example.nihongo_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.entity.QuestDefinition;
import com.example.nihongo_app.entity.QuestDefinition.QuestType;
import com.example.nihongo_app.entity.UserDailyQuest;
import com.example.nihongo_app.repository.QuestDefinitionRepository;
import com.example.nihongo_app.repository.UserDailyQuestRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyQuestServiceTest {

    @Mock
    private QuestDefinitionRepository questDefinitionRepository;

    @Mock
    private UserDailyQuestRepository userDailyQuestRepository;

    private DailyQuestService dailyQuestService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        dailyQuestService = new DailyQuestService(questDefinitionRepository, userDailyQuestRepository);
    }

    @Test
    void ensureTodayQuests_noExisting_createsThreeFromActivePool() {
        when(userDailyQuestRepository.findAllByUserIdAndQuestDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of());
        when(questDefinitionRepository.findAllByActiveTrue()).thenReturn(List.of(
                QuestDefinition.builder().id(1L).title("Q1").questType(QuestType.COMPLETE_LESSONS).targetValue(1).active(true).build(),
                QuestDefinition.builder().id(2L).title("Q2").questType(QuestType.COMPLETE_LESSONS).targetValue(2).active(true).build(),
                QuestDefinition.builder().id(3L).title("Q3").questType(QuestType.CORRECT_ANSWERS).targetValue(10).active(true).build(),
                QuestDefinition.builder().id(4L).title("Q4").questType(QuestType.CORRECT_ANSWERS).targetValue(20).active(true).build(),
                QuestDefinition.builder().id(5L).title("Q5").questType(QuestType.PERFECT_LESSON).targetValue(1).active(true).build()
        ));
        when(userDailyQuestRepository.save(any(UserDailyQuest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<UserDailyQuest> result = dailyQuestService.ensureTodayQuests(USER_ID);

        assertThat(result).hasSize(3);
        verify(userDailyQuestRepository, times(3)).save(any(UserDailyQuest.class));
    }

    @Test
    void ensureTodayQuests_alreadyExists_returnsExistingWithoutCreatingMore() {
        List<UserDailyQuest> existing = List.of(
                UserDailyQuest.builder().id(1L).userId(USER_ID).questType(QuestType.COMPLETE_LESSONS)
                        .targetValue(1).currentProgress(0).completed(false).build()
        );
        when(userDailyQuestRepository.findAllByUserIdAndQuestDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(existing);

        List<UserDailyQuest> result = dailyQuestService.ensureTodayQuests(USER_ID);

        assertThat(result).isSameAs(existing);
        verify(questDefinitionRepository, never()).findAllByActiveTrue();
        verify(userDailyQuestRepository, never()).save(any());
    }

    @Test
    void recordProgress_incrementsAndCapsAtTarget_marksCompleted() {
        UserDailyQuest quest = UserDailyQuest.builder().id(1L).userId(USER_ID)
                .questType(QuestType.CORRECT_ANSWERS).targetValue(10).currentProgress(8).completed(false).build();
        when(userDailyQuestRepository.findAllByUserIdAndQuestDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of(quest));

        dailyQuestService.recordProgress(USER_ID, QuestType.CORRECT_ANSWERS, 5);

        assertThat(quest.getCurrentProgress()).isEqualTo(10);
        assertThat(quest.getCompleted()).isTrue();
        assertThat(quest.getCompletedAt()).isNotNull();
        verify(userDailyQuestRepository).save(quest);
    }

    @Test
    void recordProgress_ignoresAlreadyCompletedQuest() {
        UserDailyQuest quest = UserDailyQuest.builder().id(1L).userId(USER_ID)
                .questType(QuestType.COMPLETE_LESSONS).targetValue(1).currentProgress(1).completed(true).build();
        when(userDailyQuestRepository.findAllByUserIdAndQuestDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of(quest));

        dailyQuestService.recordProgress(USER_ID, QuestType.COMPLETE_LESSONS, 1);

        verify(userDailyQuestRepository, never()).save(any());
    }

    @Test
    void recordProgress_ignoresNonMatchingType() {
        UserDailyQuest quest = UserDailyQuest.builder().id(1L).userId(USER_ID)
                .questType(QuestType.COMPLETE_LESSONS).targetValue(2).currentProgress(0).completed(false).build();
        when(userDailyQuestRepository.findAllByUserIdAndQuestDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of(quest));

        dailyQuestService.recordProgress(USER_ID, QuestType.CORRECT_ANSWERS, 5);

        verify(userDailyQuestRepository, never()).save(any());
    }

    @Test
    void areAllTodayQuestsCompleted_trueOnlyWhenAllDone() {
        when(userDailyQuestRepository.findAllByUserIdAndQuestDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of());
        assertThat(dailyQuestService.areAllTodayQuestsCompleted(USER_ID)).isFalse();

        UserDailyQuest done = UserDailyQuest.builder().completed(true).build();
        UserDailyQuest notDone = UserDailyQuest.builder().completed(false).build();
        when(userDailyQuestRepository.findAllByUserIdAndQuestDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of(done, notDone));
        assertThat(dailyQuestService.areAllTodayQuestsCompleted(USER_ID)).isFalse();

        when(userDailyQuestRepository.findAllByUserIdAndQuestDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of(done, done));
        assertThat(dailyQuestService.areAllTodayQuestsCompleted(USER_ID)).isTrue();
    }
}
