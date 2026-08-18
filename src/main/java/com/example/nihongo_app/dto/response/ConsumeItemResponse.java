package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.ShopItem;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsumeItemResponse {

    String itemName;
    ShopItem.EffectType effectType;
    String effectDescription;
    Integer currentCoins;
    Integer currentEnergy;
    Integer streakFreezeCount;
    String message;
}