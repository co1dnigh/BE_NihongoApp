package com.example.nihongo_app.entity;

import com.example.nihongo_app.converter.JsonNodeConverter;
import tools.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    public enum LessonType {
        NORMAL, TIMED_REVIEW, JUMP_TEST
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false)
    private LessonType lessonType;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "config_json", columnDefinition = "json")
    private JsonNode configJson;

    /**
     * Liên kết ngược về {@link Topic} để JPQL {@code JOIN FETCH t.lessons} hoạt động.
     * Không insert/update vì cột {@code topic_id} đã được ghi qua trường {@link #topicId}.
     * Chỉ dùng để JPA hydrate quan hệ khi vẽ bản đồ lộ trình.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;
}
