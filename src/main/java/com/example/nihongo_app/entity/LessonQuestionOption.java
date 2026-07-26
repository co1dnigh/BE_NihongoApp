package com.example.nihongo_app.entity;

import com.example.nihongo_app.converter.JsonNodeConverter;
import tools.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
@Table(name = "lesson_question_options")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "option_text", columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Column(name = "is_correct", nullable = false)
    private Boolean correct;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "metadata_json", columnDefinition = "json")
    private JsonNode metadataJson;
}