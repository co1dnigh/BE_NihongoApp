package com.nihongoapp.wallet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_type", "gacha_item_id"}))
public class UserInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

    @Column(name = "gacha_item_id")
    private Long gachaItemId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "acquired_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime acquiredAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }
    public Long getGachaItemId() { return gachaItemId; }
    public void setGachaItemId(Long gachaItemId) { this.gachaItemId = gachaItemId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public java.time.LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(java.time.LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }

    public enum ItemType { GACHA_ITEM, STREAK_FREEZE, HEART_REFILL }
}
