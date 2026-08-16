package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.ShopItem;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShopItemResponse {

Long id;
String name;
String description;
ShopItem.ItemType itemType;
ShopItem.EffectType effectType;
Integer effectValue;
Integer priceCoins;
Integer priceGems;
String iconUrl;
Integer sortOrder;
Boolean limitedTime;
LocalDateTime availableFrom;
LocalDateTime availableUntil;
}
