package com.nihongoapp.gacha.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gacha_items")
public class GachaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "banner_id", nullable = false)
    private Long bannerId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false, length = 5)
    private Rarity rarity;

    @Column(name = "drop_weight", nullable = false)
    private Integer dropWeight;

    @Column(name = "effect_type", nullable = false, length = 20)
    private String effectType = "GACHA_ITEM";

    @Column(name = "effect_value", nullable = false)
    private Integer effectValue = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBannerId() { return bannerId; }
    public void setBannerId(Long bannerId) { this.bannerId = bannerId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
    public Integer getDropWeight() { return dropWeight; }
    public void setDropWeight(Integer dropWeight) { this.dropWeight = dropWeight; }
    public String getEffectType() { return effectType; }
    public void setEffectType(String effectType) { this.effectType = effectType; }
    public Integer getEffectValue() { return effectValue; }
    public void setEffectValue(Integer effectValue) { this.effectValue = effectValue; }

    public enum Rarity { R, SR, SSR }
}
