package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.LessonRequest;
import com.example.nihongo_app.dto.response.LessonAdminResponse;
import com.example.nihongo_app.dto.response.LessonDetailResponse;
import com.example.nihongo_app.dto.response.LessonSummaryResponse;
import java.util.List;

public interface LessonService {

    List<LessonSummaryResponse> getLessonsByLevel(String email, String jlptLevel);

    LessonDetailResponse getLessonDetail(String email, Long lessonId);

    void completeLesson(String email, Long lessonId);

    LessonAdminResponse createLesson(LessonRequest request);

    LessonAdminResponse updateLesson(Long id, LessonRequest request);

    void deleteLesson(Long id);
}
