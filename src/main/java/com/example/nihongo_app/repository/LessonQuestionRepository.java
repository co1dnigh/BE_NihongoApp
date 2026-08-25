package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.LessonQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonQuestionRepository extends JpaRepository<LessonQuestion, Long> {

    /**
     * Lấy toàn bộ câu hỏi của 1 lesson, sắp theo {@code id ASC} để shuffle
     * ổn định (cùng lesson → cùng thứ tự gốc trước khi trộn).
     */
    List<LessonQuestion> findAllByLessonIdOrderByIdAsc(Long lessonId);

    /**
     * Pool fallback cho bài TOPIC_REVIEW khi user chưa có đủ từ đến hạn (SM-2) để lấp đầy
     * {@code questionsPerSession} — lấy toàn bộ câu hỏi NORMAL trong phạm vi topic đã học.
     */
    List<LessonQuestion> findAllByLessonIdInOrderByIdAsc(List<Long> lessonIds);
}
