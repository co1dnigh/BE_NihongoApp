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
}
