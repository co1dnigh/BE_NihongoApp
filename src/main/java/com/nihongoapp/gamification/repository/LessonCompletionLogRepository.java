package com.nihongoapp.gamification.repository;

import com.nihongoapp.gamification.model.LessonCompletionLog;
import com.nihongoapp.gamification.model.LessonCompletionLogId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonCompletionLogRepository extends JpaRepository<LessonCompletionLog, LessonCompletionLogId> {
}
