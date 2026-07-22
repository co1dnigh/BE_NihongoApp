package com.example.nihongo_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyRequest {

    @NotNull
    private Long lessonId;

    @Size(max = 100)
    private String kanji;

    @Size(max = 100)
    private String furigana;

    @Size(max = 100)
    private String romaji;

    @NotBlank
    @Size(max = 255)
    private String meaningVn;

    private String exampleSentence;

    private String exampleMeaning;

    @Size(max = 255)
    private String audioUrl;
}
