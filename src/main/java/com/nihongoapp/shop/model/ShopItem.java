package com.nihongoapp.shop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "shop_items")
public class ShopItem {

    public enum EffectType { HEART_REFILL, STREAK_FREEZE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false, length = 20)
    private EffectType effectType;

    @Column(name = "effect_value", nullable = false)
    private Integer effectValue;

    @Column(name = "price_gemstone", nullable = false)
    private Integer priceGemstone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public EffectType getEffectType() { return effectType; }
    public void setEffectType(EffectType effectType) { this.effectType = effectType; }
    public Integer getEffectValue() { return effectValue; }
    public void setEffectValue(Integer effectValue) { this.effectValue = effectValue; }
    public Integer getPriceGemstone() { return priceGemstone; }
    public void setPriceGemstone(Integer priceGemstone) { this.priceGemstone = priceGemstone; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
