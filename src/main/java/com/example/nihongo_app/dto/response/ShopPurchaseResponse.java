package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.ShopItem;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShopPurchaseResponse {

    Long inventoryId;
    String itemName;
    ShopItem.EffectType effectType;
    Integer coinsSpent;
    Integer currentCoins;
    String message;
}