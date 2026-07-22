package com.example.nihongo_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyResponse {

    private Long id;
    private String kanji;
    private String furigana;
    private String romaji;
    private String meaningVn;
    private String exampleSentence;
    private String exampleMeaning;
    private String audioUrl;
}
