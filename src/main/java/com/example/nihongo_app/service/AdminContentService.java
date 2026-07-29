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
    List<TopicWithLessonsResponse> getAllTopics();
    TopicWithLessonsResponse getTopicById(Long id);

    LessonResponse createLesson(CreateLessonRequest request);
    LessonWithQuestionsResponse getLessonById(Long id);

    QuestionResponse createQuestion(CreateQuestionRequest request);
    QuestionWithOptionsResponse getQuestionById(Long id);
}
