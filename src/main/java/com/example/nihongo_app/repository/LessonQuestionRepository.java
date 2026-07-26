package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.LessonQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonQuestionRepository extends JpaRepository<LessonQuestion, Long> {
}
