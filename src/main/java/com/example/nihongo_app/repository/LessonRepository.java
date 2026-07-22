package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Lesson;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByJlptLevelAndDeletedAtIsNullOrderByOrderIndexAsc(String jlptLevel);

    Optional<Lesson> findByIdAndDeletedAtIsNull(Long id);
}
