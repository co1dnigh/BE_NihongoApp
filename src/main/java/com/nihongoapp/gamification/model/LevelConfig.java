package com.nihongoapp.gamification.model;

import jakarta.persistence.*;

@Entity
@Table(name = "level_config")
public class LevelConfig {

    @Id
    @Column(name = "level")
    private Integer level;

    @Column(name = "xp_required", nullable = false)
    private Integer xpRequired;

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getXpRequired() { return xpRequired; }
    public void setXpRequired(Integer xpRequired) { this.xpRequired = xpRequired; }
}
