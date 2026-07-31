package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.CreateLessonRequest;
import com.example.nihongo_app.dto.request.CreateQuestionRequest;
import com.example.nihongo_app.dto.request.CreateTopicRequest;
import com.example.nihongo_app.dto.request.QuestionOptionRequest;
import com.example.nihongo_app.dto.response.LessonResponse;
import com.example.nihongo_app.dto.response.LessonWithQuestionsResponse;
import com.example.nihongo_app.dto.response.QuestionResponse;
import com.example.nihongo_app.dto.response.QuestionWithOptionsResponse;
import com.example.nihongo_app.dto.response.TopicResponse;
import com.example.nihongo_app.dto.response.TopicWithLessonsResponse;
import com.example.nihongo_app.entity.Lesson;
import com.example.nihongo_app.entity.LessonQuestion;
import com.example.nihongo_app.entity.LessonQuestionOption;
import com.example.nihongo_app.entity.Topic;
import com.example.nihongo_app.repository.LessonQuestionOptionRepository;
import com.example.nihongo_app.repository.LessonQuestionRepository;
import com.example.nihongo_app.repository.LessonRepository;
import com.example.nihongo_app.repository.TopicRepository;
import com.example.nihongo_app.service.AdminContentService;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final LessonQuestionRepository questionRepository;
    private final LessonQuestionOptionRepository optionRepository;

    @Override
    @Transactional
    public TopicResponse createTopic(CreateTopicRequest request) {
        Topic topic = topicRepository.save(Topic.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .build());
        return toTopicResponse(topic);
    }

    @Override
    @Transactional
    public TopicResponse updateTopic(Long id, CreateTopicRequest request) {
        Topic topic = topicRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> notFound("Topic not found: " + id));
        topic.setTitle(request.getTitle().trim());
        topic.setDescription(request.getDescription());
        topic.setOrderIndex(request.getOrderIndex());
        return toTopicResponse(topicRepository.save(topic));
    }

    @Override
    @Transactional
    public void deleteTopic(Long id) {
        Topic topic = topicRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> notFound("Topic not found: " + id));
        topic.setDeletedAt(LocalDateTime.now());
        topicRepository.save(topic);
    }

    @Override
    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request) {
        if (!topicRepository.existsById(request.getTopicId())) {
            throw notFound("Topic not found: " + request.getTopicId());
        }

        Lesson.LessonType type = request.getLessonType();
        Integer orderIndex = request.getOrderIndex();

        if (type == Lesson.LessonType.JUMP_TEST) {
            // JUMP_TEST không có toạ độ trên đường đi -> ép null
            orderIndex = null;
        } else if (orderIndex == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "orderIndex is required for " + type + " lessons");
        }

        Lesson lesson = lessonRepository.save(Lesson.builder()
                .topicId(request.getTopicId())
                .title(request.getTitle() == null ? null : request.getTitle().trim())
                .lessonType(type)
                .orderIndex(orderIndex)
                .configJson(request.getConfigJson())
                .build());
        return toLessonResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(Long id, CreateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> notFound("Lesson not found: " + id));
        if (!topicRepository.existsById(request.getTopicId())) {
            throw notFound("Topic not found: " + request.getTopicId());
        }

        Integer orderIndex = request.getOrderIndex();
        if (request.getLessonType() == Lesson.LessonType.JUMP_TEST) {
            orderIndex = null;
        }

        lesson.setTopicId(request.getTopicId());
        lesson.setTitle(request.getTitle().trim());
        lesson.setLessonType(request.getLessonType());
        lesson.setOrderIndex(orderIndex);
        lesson.setConfigJson(request.getConfigJson());
        return toLessonResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw notFound("Lesson not found: " + id);
        }
        lessonRepository.deleteById(id);
    }

    @Override
    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        if (!lessonRepository.existsById(request.getLessonId())) {
            throw notFound("Lesson not found: " + request.getLessonId());
        }
        validateOptions(request.getOptions());

        LessonQuestion question = questionRepository.save(LessonQuestion.builder()
                .lessonId(request.getLessonId())
                .questionType(request.getQuestionType())
                .questionText(request.getQuestionText())
                .audioUrl(request.getAudioUrl())
                .imageUrl(request.getImageUrl())
                .metadataJson(request.getMetadataJson())
                .build());

        List<LessonQuestionOption> options = IntStream.range(0, request.getOptions().size())
                .mapToObj(index -> toOption(question.getId(), request.getOptions().get(index), index))
                .toList();
        List<LessonQuestionOption> savedOptions = optionRepository.saveAll(options);
        return toQuestionResponse(question, savedOptions);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, CreateQuestionRequest request) {
        LessonQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> notFound("Question not found: " + id));
        if (!lessonRepository.existsById(request.getLessonId())) {
            throw notFound("Lesson not found: " + request.getLessonId());
        }
        validateOptions(request.getOptions());

        question.setLessonId(request.getLessonId());
        question.setQuestionType(request.getQuestionType());
        question.setQuestionText(request.getQuestionText());
        question.setAudioUrl(request.getAudioUrl());
        question.setImageUrl(request.getImageUrl());
        question.setMetadataJson(request.getMetadataJson());
        LessonQuestion savedQuestion = questionRepository.save(question);

        optionRepository.deleteAllByQuestionId(id);
        List<LessonQuestionOption> options = IntStream.range(0, request.getOptions().size())
                .mapToObj(index -> toOption(savedQuestion.getId(), request.getOptions().get(index), index))
                .toList();
        List<LessonQuestionOption> savedOptions = optionRepository.saveAll(options);
        return toQuestionResponse(savedQuestion, savedOptions);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw notFound("Question not found: " + id);
        }
        optionRepository.deleteAllByQuestionId(id);
        questionRepository.deleteById(id);
    }

    private void validateOptions(List<QuestionOptionRequest> options) {
        if (options.stream().noneMatch(option -> Boolean.TRUE.equals(option.getIsCorrect()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one option must be correct");
        }
        options.forEach(option -> {
            boolean hasValue = (option.getOptionText() != null && !option.getOptionText().isBlank())
                    || (option.getImageUrl() != null && !option.getImageUrl().isBlank())
                    || (option.getAudioUrl() != null && !option.getAudioUrl().isBlank());
            if (!hasValue) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each option must have content");
            }
        });
    }

    private LessonQuestionOption toOption(Long questionId, QuestionOptionRequest request, int index) {
        return LessonQuestionOption.builder()
                .questionId(questionId)
                .optionText(request.getOptionText())
                .imageUrl(request.getImageUrl())
                .audioUrl(request.getAudioUrl())
                .correct(request.getIsCorrect())
                .orderIndex(request.getOrderIndex() == null ? index + 1 : request.getOrderIndex())
                .metadataJson(request.getMetadataJson())
                .build();
    }

    private TopicResponse toTopicResponse(Topic topic) {
        return TopicResponse.builder().id(topic.getId()).title(topic.getTitle())
                .description(topic.getDescription()).orderIndex(topic.getOrderIndex()).build();
    }

    private LessonResponse toLessonResponse(Lesson lesson) {
        return LessonResponse.builder().id(lesson.getId()).topicId(lesson.getTopicId())
                .title(lesson.getTitle()).lessonType(lesson.getLessonType())
                .orderIndex(lesson.getOrderIndex()).configJson(lesson.getConfigJson()).build();
    }

    private QuestionResponse toQuestionResponse(LessonQuestion question, List<LessonQuestionOption> options) {
        return QuestionResponse.builder().id(question.getId()).lessonId(question.getLessonId())
                .questionType(question.getQuestionType()).questionText(question.getQuestionText())
                .audioUrl(question.getAudioUrl()).imageUrl(question.getImageUrl())
                .metadataJson(question.getMetadataJson())
                .options(options.stream().map(option -> QuestionResponse.OptionResponse.builder()
                        .id(option.getId()).optionText(option.getOptionText()).imageUrl(option.getImageUrl())
                        .audioUrl(option.getAudioUrl()).isCorrect(option.getCorrect()).orderIndex(option.getOrderIndex())
                        .metadataJson(option.getMetadataJson())
                        .build()).toList()).build();
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicWithLessonsResponse> getAllTopics() {
        return topicRepository.findAllActiveWithLessons().stream()
                .map(this::toTopicWithLessonsResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TopicWithLessonsResponse getTopicById(Long id) {
        Topic topic = topicRepository.findByIdWithLessonsOrdered(id)
                .orElseThrow(() -> notFound("Topic not found: " + id));
        return toTopicWithLessonsResponse(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponse> getAllLessons() {
        return lessonRepository.findAllActiveOrdered().stream()
                .map(this::toLessonResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonWithQuestionsResponse getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> notFound("Lesson not found: " + id));
        List<LessonQuestion> questions = questionRepository.findAllByLessonIdOrderByIdAsc(id);
        return toLessonWithQuestionsResponse(lesson, questions);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionWithOptionsResponse getQuestionById(Long id) {
        LessonQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> notFound("Question not found: " + id));
        List<LessonQuestionOption> options = optionRepository.findAllByQuestionIdOrderByOrderIndexAsc(id);
        return toQuestionWithOptionsResponse(question, options);
    }

    private TopicWithLessonsResponse toTopicWithLessonsResponse(Topic topic) {
        List<TopicWithLessonsResponse.LessonSummaryResponse> lessonSummaries =
                topic.getLessons().stream()
                        .sorted((a, b) -> {
                            if (a.getOrderIndex() == null && b.getOrderIndex() == null) return a.getId().compareTo(b.getId());
                            if (a.getOrderIndex() == null) return 1;
                            if (b.getOrderIndex() == null) return -1;
                            return a.getOrderIndex().compareTo(b.getOrderIndex());
                        })
                        .map(lesson -> TopicWithLessonsResponse.LessonSummaryResponse.builder()
                                .id(lesson.getId())
                                .title(lesson.getTitle())
                                .lessonType(lesson.getLessonType().name())
                                .orderIndex(lesson.getOrderIndex())
                                .questionCount((int) questionRepository.findAllByLessonIdOrderByIdAsc(lesson.getId()).size())
                                .build())
                        .toList();

        return TopicWithLessonsResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .orderIndex(topic.getOrderIndex())
                .lessons(lessonSummaries)
                .build();
    }

    private LessonWithQuestionsResponse toLessonWithQuestionsResponse(Lesson lesson, List<LessonQuestion> questions) {
        List<LessonWithQuestionsResponse.QuestionSummaryResponse> questionSummaries = questions.stream()
                .map(q -> {
                    List<LessonQuestionOption> options = optionRepository.findAllByQuestionIdOrderByOrderIndexAsc(q.getId());
                    int correctCount = (int) options.stream().filter(LessonQuestionOption::getCorrect).count();
                    return LessonWithQuestionsResponse.QuestionSummaryResponse.builder()
                            .id(q.getId())
                            .questionType(q.getQuestionType().name())
                            .questionText(q.getQuestionText())
                            .audioUrl(q.getAudioUrl())
                            .imageUrl(q.getImageUrl())
                            .optionCount(options.size())
                            .correctCount(correctCount)
                            .build();
                }).toList();

        return LessonWithQuestionsResponse.builder()
                .id(lesson.getId())
                .topicId(lesson.getTopicId())
                .title(lesson.getTitle())
                .lessonType(lesson.getLessonType().name())
                .orderIndex(lesson.getOrderIndex())
                .configJson(lesson.getConfigJson())
                .questions(questionSummaries)
                .build();
    }

    private QuestionWithOptionsResponse toQuestionWithOptionsResponse(LessonQuestion question, List<LessonQuestionOption> options) {
        return QuestionWithOptionsResponse.builder()
                .id(question.getId())
                .lessonId(question.getLessonId())
                .questionType(question.getQuestionType().name())
                .questionText(question.getQuestionText())
                .audioUrl(question.getAudioUrl())
                .imageUrl(question.getImageUrl())
                .metadataJson(question.getMetadataJson())
                .options(options.stream()
                        .map(opt -> QuestionWithOptionsResponse.OptionResponse.builder()
                                .id(opt.getId())
                                .optionText(opt.getOptionText())
                                .imageUrl(opt.getImageUrl())
                                .audioUrl(opt.getAudioUrl())
                                .isCorrect(opt.getCorrect())
                                .orderIndex(opt.getOrderIndex())
                                .metadataJson(opt.getMetadataJson())
                                .build())
                        .toList())
                .build();
    }

@Override
    public List<QuestionWithOptionsResponse> getAllQuestions() {
        // 1. Lấy toàn bộ câu hỏi từ Database
        List<LessonQuestion> questions = questionRepository.findAll();
        
        // 2. Lặp qua từng câu hỏi, lấy các option đi kèm và map sang DTO
        return questions.stream()
                .map(q -> {
                    List<LessonQuestionOption> options = optionRepository.findAllByQuestionIdOrderByOrderIndexAsc(q.getId());
                    return toQuestionWithOptionsResponse(q, options);
                })
                .toList();
    }
} // Chú ý giữ lại dấu ngoặc nhọn đóng của class ở cuối cùng nhé
    
