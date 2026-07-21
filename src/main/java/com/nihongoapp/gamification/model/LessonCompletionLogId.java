package com.nihongoapp.gamification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class LessonCompletionLogId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "lesson_id")
    private Long lessonId;

    public LessonCompletionLogId() {}
    public LessonCompletionLogId(Long userId, Long lessonId) {
        this.userId = userId;
        this.lessonId = lessonId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LessonCompletionLogId)) return false;
        LessonCompletionLogId that = (LessonCompletionLogId) o;
        return userId != null && userId.equals(that.userId)
            && lessonId != null && lessonId.equals(that.lessonId);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
