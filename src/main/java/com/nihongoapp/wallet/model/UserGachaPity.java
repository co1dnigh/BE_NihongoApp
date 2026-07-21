package com.nihongoapp.wallet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_gacha_pity")
public class UserGachaPity {

  @EmbeddedId
  private UserGachaPityId id = new UserGachaPityId();

  @MapsId("userId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private com.nihongoapp.user.model.User user;

  @Column(name = "spin_count_since_last_ssr", nullable = false)
  private Integer spinCountSinceLastSsr = 0;

  public UserGachaPity() {}

  public UserGachaPity(com.nihongoapp.user.model.User user, Long bannerId) {
    this.id = new UserGachaPityId(user.getId(), bannerId);
    this.user = user;
  }

  public UserGachaPityId getId() { return id; }
  public void setId(UserGachaPityId id) { this.id = id; }
  public com.nihongoapp.user.model.User getUser() { return user; }
  public void setUser(com.nihongoapp.user.model.User user) { this.user = user; }
  public Integer getSpinCountSinceLastSsr() { return spinCountSinceLastSsr; }
  public void setSpinCountSinceLastSsr(Integer spinCountSinceLastSsr) { this.spinCountSinceLastSsr = spinCountSinceLastSsr; }
}
