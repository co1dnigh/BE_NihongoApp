package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.ShopItem;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class InventoryItemResponse {

    Long inventoryId;
    Long itemId;
    String name;
    String description;
    ShopItem.ItemType itemType;
    ShopItem.EffectType effectType;
    Integer effectValue;
    Integer quantity;
    Boolean equipped;
    Boolean active;
    String iconUrl;
    LocalDateTime expiresAt;
    LocalDateTime acquiredAt;
}