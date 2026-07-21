package com.nihongoapp.wallet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_wallet")
public class UserWallet {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "gem_balance", nullable = false)
    private Long gemBalance = 0L;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getGemBalance() { return gemBalance; }
    public void setGemBalance(Long gemBalance) { this.gemBalance = gemBalance; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
