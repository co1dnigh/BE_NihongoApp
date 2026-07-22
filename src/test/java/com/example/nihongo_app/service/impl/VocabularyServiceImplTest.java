package com.example.nihongo_app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.request.VocabularyRequest;
import com.example.nihongo_app.dto.response.VocabularyAdminResponse;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.Vocabulary;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.VocabularyRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class VocabularyServiceImplTest {

    @Mock
    private VocabularyRepository vocabularyRepository;
    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private VocabularyServiceImpl vocabularyService;

    private VocabularyRequest sampleRequest() {
        return VocabularyRequest.builder()
                .lessonId(1L)
                .kanji("こんにちは")
                .furigana("こんにちは")
                .romaji("konnichiwa")
                .meaningVn("Xin chao")
                .build();
    }

    @Test
    void createVocabulary_throwsNotFound_whenLessonMissing() {
        when(lessonRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vocabularyService.createVocabulary(sampleRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(vocabularyRepository, never()).save(any());
    }

    @Test
    void createVocabulary_success() {
        when(lessonRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(Lesson.builder().id(1L).build()));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(invocation -> {
            Vocabulary saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        VocabularyAdminResponse response = vocabularyService.createVocabulary(sampleRequest());

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getLessonId()).isEqualTo(1L);
        assertThat(response.getMeaningVn()).isEqualTo("Xin chao");
    }

    @Test
    void updateVocabulary_throwsNotFound_whenVocabMissing() {
        when(vocabularyRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vocabularyService.updateVocabulary(5L, sampleRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateVocabulary_success() {
        Vocabulary existing = Vocabulary.builder().id(5L).lessonId(1L).meaningVn("old").build();
        when(vocabularyRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(existing));
        when(lessonRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(Lesson.builder().id(1L).build()));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VocabularyAdminResponse response = vocabularyService.updateVocabulary(5L, sampleRequest());

        assertThat(response.getMeaningVn()).isEqualTo("Xin chao");
    }

    @Test
    void deleteVocabulary_setsDeletedAt() {
        Vocabulary existing = Vocabulary.builder().id(5L).lessonId(1L).build();
        when(vocabularyRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(existing));

        vocabularyService.deleteVocabulary(5L);

        ArgumentCaptor<Vocabulary> captor = ArgumentCaptor.forClass(Vocabulary.class);
        verify(vocabularyRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }
}
