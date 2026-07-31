package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.CreateLessonRequest;
import com.example.nihongo_app.dto.request.CreateQuestionRequest;
import com.example.nihongo_app.dto.request.CreateTopicRequest;
import com.example.nihongo_app.dto.response.LessonResponse;
import com.example.nihongo_app.dto.response.LessonWithQuestionsResponse;
import com.example.nihongo_app.dto.response.QuestionWithOptionsResponse;
import com.example.nihongo_app.dto.response.QuestionResponse;
import com.example.nihongo_app.dto.response.TopicResponse;
import com.example.nihongo_app.dto.response.TopicWithLessonsResponse;
import java.util.List;

public interface AdminContentService {

    TopicResponse createTopic(CreateTopicRequest request);
    TopicResponse updateTopic(Long id, CreateTopicRequest request);
    void deleteTopic(Long id);
    List<TopicWithLessonsResponse> getAllTopics();
    TopicWithLessonsResponse getTopicById(Long id);

    LessonResponse createLesson(CreateLessonRequest request);
    LessonResponse updateLesson(Long id, CreateLessonRequest request);
    void deleteLesson(Long id);
    List<LessonResponse> getAllLessons();
    LessonWithQuestionsResponse getLessonById(Long id);

    QuestionResponse createQuestion(CreateQuestionRequest request);
    QuestionResponse updateQuestion(Long id, CreateQuestionRequest request);
    void deleteQuestion(Long id);
    QuestionWithOptionsResponse getQuestionById(Long id);
    
    List<QuestionWithOptionsResponse> getAllQuestions();
}
