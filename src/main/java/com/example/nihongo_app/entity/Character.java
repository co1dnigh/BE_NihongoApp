package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
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
@Table(name = "characters")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Character {

    public enum CharacterType {
        HIRAGANA, KATAKANA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, length = 50)
    private String romaji;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CharacterType type;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "stroke_order_data", columnDefinition = "TEXT")
    private String strokeOrderData;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}