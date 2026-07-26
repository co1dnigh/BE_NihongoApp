package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.CreateLessonRequest;
import com.example.nihongo_app.dto.request.CreateQuestionRequest;
import com.example.nihongo_app.dto.request.CreateTopicRequest;
import com.example.nihongo_app.dto.response.LessonResponse;
import com.example.nihongo_app.dto.response.QuestionResponse;
import com.example.nihongo_app.dto.response.TopicResponse;

public interface AdminContentService {

    TopicResponse createTopic(CreateTopicRequest request);

    LessonResponse createLesson(CreateLessonRequest request);

    QuestionResponse createQuestion(CreateQuestionRequest request);
}
