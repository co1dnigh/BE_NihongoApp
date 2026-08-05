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
 * Log ghi nhận thay đổi số dư coin của user.
 * Bảng vật lý: {@code coin_transactions} (tạo bởi V4__create_gamification_tables.sql,
 * bổ sung 2 transaction_type mới bởi V21__add_chest_tracking_and_coin_types.sql).
 */
@Entity
@Table(name = "coin_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinTransaction {

    public enum TransactionType {
        EARN_LESSON, BUY_ITEM, STREAK_BONUS, ADMIN_ADJUST, DAILY_CHEST, BUY_STREAK_FREEZE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Âm là trừ tiền, dương là cộng tiền. */
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    /** ID tham chiếu tuỳ ngữ cảnh: lesson_id (EARN_LESSON), null cho DAILY_CHEST/STREAK_BONUS. */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
