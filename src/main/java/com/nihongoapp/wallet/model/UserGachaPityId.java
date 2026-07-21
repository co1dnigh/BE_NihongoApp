package com.nihongoapp.wallet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserGachaPityId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "banner_id", nullable = false)
    private Long bannerId;

    public UserGachaPityId() {}
    public UserGachaPityId(Long userId, Long bannerId) {
        this.userId = userId;
        this.bannerId = bannerId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBannerId() { return bannerId; }
    public void setBannerId(Long bannerId) { this.bannerId = bannerId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGachaPityId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(bannerId, that.bannerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, bannerId);
    }
}
