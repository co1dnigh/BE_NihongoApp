package com.nihongoapp.wallet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false, length = 10)
    private CurrencyType currencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 20)
    private WalletTransactionReason reason;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "idempotency_key", nullable = false, length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public CurrencyType getCurrencyType() { return currencyType; }
    public void setCurrencyType(CurrencyType currencyType) { this.currencyType = currencyType; }
    public WalletTransactionReason getReason() { return reason; }
    public void setReason(WalletTransactionReason reason) { this.reason = reason; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Long getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Long balanceAfter) { this.balanceAfter = balanceAfter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
