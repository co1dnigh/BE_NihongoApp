package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.VocabularyRequest;
import com.example.nihongo_app.dto.response.VocabularyAdminResponse;
import com.example.nihongo_app.entity.Vocabulary;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.VocabularyRepository;
import com.example.nihongo_app.service.VocabularyService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public VocabularyAdminResponse createVocabulary(VocabularyRequest request) {
        lessonRepository.findByIdAndDeletedAtIsNull(request.getLessonId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        Vocabulary vocabulary = Vocabulary.builder()
                .lessonId(request.getLessonId())
                .kanji(request.getKanji())
                .furigana(request.getFurigana())
                .romaji(request.getRomaji())
                .meaningVn(request.getMeaningVn())
                .exampleSentence(request.getExampleSentence())
                .exampleMeaning(request.getExampleMeaning())
                .audioUrl(request.getAudioUrl())
                .build();
        return toAdminResponse(vocabularyRepository.save(vocabulary));
    }

    @Override
    @Transactional
    public VocabularyAdminResponse updateVocabulary(Long id, VocabularyRequest request) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vocabulary not found"));

        lessonRepository.findByIdAndDeletedAtIsNull(request.getLessonId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        vocabulary.setLessonId(request.getLessonId());
        vocabulary.setKanji(request.getKanji());
        vocabulary.setFurigana(request.getFurigana());
        vocabulary.setRomaji(request.getRomaji());
        vocabulary.setMeaningVn(request.getMeaningVn());
        vocabulary.setExampleSentence(request.getExampleSentence());
        vocabulary.setExampleMeaning(request.getExampleMeaning());
        vocabulary.setAudioUrl(request.getAudioUrl());
        return toAdminResponse(vocabularyRepository.save(vocabulary));
    }

    @Override
    @Transactional
    public void deleteVocabulary(Long id) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vocabulary not found"));

        vocabulary.setDeletedAt(LocalDateTime.now());
        vocabularyRepository.save(vocabulary);
    }

    private VocabularyAdminResponse toAdminResponse(Vocabulary vocabulary) {
        return VocabularyAdminResponse.builder()
                .id(vocabulary.getId())
                .lessonId(vocabulary.getLessonId())
                .kanji(vocabulary.getKanji())
                .furigana(vocabulary.getFurigana())
                .romaji(vocabulary.getRomaji())
                .meaningVn(vocabulary.getMeaningVn())
                .exampleSentence(vocabulary.getExampleSentence())
                .exampleMeaning(vocabulary.getExampleMeaning())
                .audioUrl(vocabulary.getAudioUrl())
                .build();
    }
}
