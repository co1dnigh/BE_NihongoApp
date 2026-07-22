package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.LessonRequest;
import com.example.nihongo_app.dto.response.ExercisePlayResponse;
import com.example.nihongo_app.dto.response.LessonAdminResponse;
import com.example.nihongo_app.dto.response.LessonDetailResponse;
import com.example.nihongo_app.dto.response.LessonSummaryResponse;
import com.example.nihongo_app.dto.response.VocabularyResponse;
import com.example.nihongo_app.entity.Exercise;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserLessonProgress;
import com.example.nihongo_app.entity.Vocabulary;
import com.example.nihongo_app.repository.ExerciseRepository;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.UserLessonProgressRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.repository.VocabularyRepository;
import com.example.nihongo_app.service.LessonService;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private static final String STATUS_LOCKED = "LOCKED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LessonSummaryResponse> getLessonsByLevel(String email, String jlptLevel) {
        User currentUser = getUserByEmail(email);
        List<Lesson> lessons = lessonRepository.findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc(jlptLevel);
        Map<Long, String> statusByLessonId = computeStatuses(currentUser.getId(), lessons);

        return lessons.stream()
                .map(lesson -> LessonSummaryResponse.builder()
                        .id(lesson.getId())
                        .jlptLevel(lesson.getJlptLevel())
                        .title(lesson.getTitle())
                        .orderIndex(lesson.getOrderIndex())
                        .status(statusByLessonId.get(lesson.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LessonDetailResponse getLessonDetail(String email, Long lessonId) {
        User currentUser = getUserByEmail(email);
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        List<Lesson> lessonsInLevel = lessonRepository
                .findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc(lesson.getJlptLevel());
        Map<Long, String> statusByLessonId = computeStatuses(currentUser.getId(), lessonsInLevel);
        String status = statusByLessonId.get(lesson.getId());

        if (STATUS_LOCKED.equals(status)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lesson is locked");
        }

        UserLessonProgress progress = userLessonProgressRepository
                .findByUserIdAndLessonId(currentUser.getId(), lesson.getId())
                .orElse(null);
        if (progress == null) {
            progress = UserLessonProgress.builder()
                    .userId(currentUser.getId())
                    .lessonId(lesson.getId())
                    .status(STATUS_IN_PROGRESS)
                    .unlockedAt(LocalDateTime.now())
                    .build();
            progress = userLessonProgressRepository.save(progress);
        }

        List<VocabularyResponse> vocabularies = vocabularyRepository
                .findByLessonIdAndDeletedAtIsNullOrderByIdAsc(lesson.getId()).stream()
                .map(this::toVocabularyResponse)
                .collect(Collectors.toList());

        List<ExercisePlayResponse> exercises = exerciseRepository
                .findByLessonIdOrderByIdAsc(lesson.getId()).stream()
                .map(this::toExercisePlayResponse)
                .collect(Collectors.toList());

        return LessonDetailResponse.builder()
                .id(lesson.getId())
                .jlptLevel(lesson.getJlptLevel())
                .title(lesson.getTitle())
                .orderIndex(lesson.getOrderIndex())
                .status(progress.getStatus())
                .vocabularies(vocabularies)
                .exercises(exercises)
                .build();
    }

    @Override
    @Transactional
    public void completeLesson(String email, Long lessonId) {
        User currentUser = getUserByEmail(email);
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        List<Lesson> lessonsInLevel = lessonRepository
                .findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc(lesson.getJlptLevel());
        Map<Long, String> statusByLessonId = computeStatuses(currentUser.getId(), lessonsInLevel);
        if (STATUS_LOCKED.equals(statusByLessonId.get(lesson.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lesson is locked");
        }

        UserLessonProgress progress = userLessonProgressRepository
                .findByUserIdAndLessonId(currentUser.getId(), lesson.getId())
                .orElseGet(() -> UserLessonProgress.builder()
                        .userId(currentUser.getId())
                        .lessonId(lesson.getId())
                        .unlockedAt(LocalDateTime.now())
                        .build());

        progress.setStatus(STATUS_COMPLETED);
        userLessonProgressRepository.save(progress);
    }

    @Override
    @Transactional
    public LessonAdminResponse createLesson(LessonRequest request) {
        Lesson lesson = Lesson.builder()
                .jlptLevel(request.getJlptLevel())
                .title(request.getTitle())
                .orderIndex(request.getOrderIndex())
                .build();
        return toAdminResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonAdminResponse updateLesson(Long id, LessonRequest request) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        lesson.setJlptLevel(request.getJlptLevel());
        lesson.setTitle(request.getTitle());
        lesson.setOrderIndex(request.getOrderIndex());
        return toAdminResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        lesson.setDeletedAt(LocalDateTime.now());
        lessonRepository.save(lesson);
    }

    private Map<Long, String> computeStatuses(Long userId, List<Lesson> lessonsOrderedByIndex) {
        List<Long> lessonIds = lessonsOrderedByIndex.stream().map(Lesson::getId).collect(Collectors.toList());
        Map<Long, String> progressByLessonId = userLessonProgressRepository
                .findByUserIdAndLessonIdIn(userId, lessonIds).stream()
                .collect(Collectors.toMap(UserLessonProgress::getLessonId, UserLessonProgress::getStatus));

        Map<Long, String> statusByLessonId = new LinkedHashMap<>();
        String previousStatus = null;
        for (Lesson lesson : lessonsOrderedByIndex) {
            String status = progressByLessonId.get(lesson.getId());
            if (status == null) {
                boolean unlocked = previousStatus == null || STATUS_COMPLETED.equals(previousStatus);
                status = unlocked ? STATUS_IN_PROGRESS : STATUS_LOCKED;
            }
            statusByLessonId.put(lesson.getId(), status);
            previousStatus = status;
        }
        return statusByLessonId;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private VocabularyResponse toVocabularyResponse(Vocabulary vocabulary) {
        return VocabularyResponse.builder()
                .id(vocabulary.getId())
                .kanji(vocabulary.getKanji())
                .furigana(vocabulary.getFurigana())
                .romaji(vocabulary.getRomaji())
                .meaningVn(vocabulary.getMeaningVn())
                .exampleSentence(vocabulary.getExampleSentence())
                .exampleMeaning(vocabulary.getExampleMeaning())
                .audioUrl(vocabulary.getAudioUrl())
                .build();
    }

    private ExercisePlayResponse toExercisePlayResponse(Exercise exercise) {
        return ExercisePlayResponse.builder()
                .id(exercise.getId())
                .exerciseType(exercise.getExerciseType())
                .questionText(exercise.getQuestionText())
                .options(parseOptions(exercise.getOptionsJson()))
                .build();
    }

    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        return objectMapper.readValue(optionsJson, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, String.class));
    }

    private LessonAdminResponse toAdminResponse(Lesson lesson) {
        return LessonAdminResponse.builder()
                .id(lesson.getId())
                .jlptLevel(lesson.getJlptLevel())
                .title(lesson.getTitle())
                .orderIndex(lesson.getOrderIndex())
                .build();
    }
}
