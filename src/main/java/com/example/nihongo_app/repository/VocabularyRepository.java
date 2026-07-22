package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Vocabulary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    List<Vocabulary> findByLessonIdAndDeletedAtIsNullOrderByIdAsc(Long lessonId);

    Optional<Vocabulary> findByIdAndDeletedAtIsNull(Long id);
}
