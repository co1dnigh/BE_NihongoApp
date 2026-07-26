package com.example.nihongo_app.entity;

import com.example.nihongo_app.converter.JsonNodeConverter;
import tools.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lesson_questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonQuestion {

    public enum QuestionType {
        SELECT_IMAGE, TRANSLATE_TO_JP, TRANSLATE_TO_VN, LISTEN_AND_ARRANGE,
        LISTEN_AND_SELECT, SPEAKING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "metadata_json", columnDefinition = "json")
    private JsonNode metadataJson;
}