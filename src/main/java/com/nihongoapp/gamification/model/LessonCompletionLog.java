package com.nihongoapp.gamification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_completion_log")
public class LessonCompletionLog {

    @EmbeddedId
    private LessonCompletionLogId id;

    @Column(name = "completed_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime completedAt = LocalDateTime.now();

    public LessonCompletionLogId getId() { return id; }
    public void setId(LessonCompletionLogId id) { this.id = id; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
