package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Exercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByLessonIdOrderByIdAsc(Long lessonId);
}
