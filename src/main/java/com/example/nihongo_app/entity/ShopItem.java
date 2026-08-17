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
 * Mặt hàng trong Shop (catalog).
 * Bảng vật lý: {@code shop_items} (tạo bởi V24__create_shop_tables.sql).
 */
@Entity
@Table(name = "shop_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItem {

    public enum ItemType {
        CONSUMABLE,  // Tiêu hao ngay lập tức (streak freeze, energy refill)
        POWERUP,     // Có thời hạn, tác động gameplay (double xp, double coin, timer boost)
        COSMETIC     // Chỉ hiển thị (avatar frame, badge, theme)
    }

    public enum EffectType {
        STREAK_FREEZE,
        ENERGY_REFILL,
        DOUBLE_XP,
        DOUBLE_COIN,
        TIMER_BOOST,
        AVATAR_FRAME,
        BADGE,
        THEME
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false)
    private EffectType effectType;

    @Column(name = "effect_value")
    private Integer effectValue;

    @Column(name = "price_coins", nullable = false)
    private Integer priceCoins;

    @Column(name = "price_gems")
    private Integer priceGems;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "limited_time", nullable = false)
    private Boolean limitedTime;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Column(name = "available_until")
    private LocalDateTime availableUntil;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Kiểm tra item có đang khả dụng để mua không (active, không bị xóa, trong khoảng thời gian limited-time nếu có).
     */
    public boolean isAvailable() {
        if (!Boolean.TRUE.equals(active) || deletedAt != null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(limitedTime)) {
            if (availableFrom != null && now.isBefore(availableFrom)) {
                return false;
            }
            if (availableUntil != null && now.isAfter(availableUntil)) {
                return false;
            }
        }
        return true;
    }
}