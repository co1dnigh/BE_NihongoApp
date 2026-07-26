package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
