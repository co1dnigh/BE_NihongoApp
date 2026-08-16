package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inventory của user - vật phẩm đã sở hữu.
 * Bảng vật lý: {@code user_inventories} (tạo bởi V4__create_gamification_tables.sql,
 * mở rộng bởi V24__create_shop_tables.sql).
 */
@Entity
@Table(name = "user_inventories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInventory {

    public enum AcquiredFrom {
        SHOP_BUY,
        CHEST_REWARD,
        QUEST_REWARD,
        SYSTEM_GIFT,
        EVENT_REWARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean equipped;

    @Enumerated(EnumType.STRING)
    @Column(name = "acquired_from", nullable = false)
    private AcquiredFrom acquiredFrom;

    @Column(name = "acquired_at", insertable = false, updatable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Kiểm tra item còn hiệu lực (chỉ áp dụng cho POWERUP có expires_at).
     */
    public boolean isActive() {
        if (expiresAt == null) {
            return true; // CONSUMABLE và COSMETIC không hết hạn
        }
        return LocalDateTime.now().isBefore(expiresAt);
    }
}