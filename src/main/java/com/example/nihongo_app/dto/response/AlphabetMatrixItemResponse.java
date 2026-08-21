package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.Character.CharacterType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AlphabetMatrixItemResponse {
    Long characterId;
    String symbol;
    String romaji;
    CharacterType type;
    String audioUrl;
    String strokeOrderData;
    Integer orderIndex;
    Integer masteryLevel;
}