package com.nihongoapp.user.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "STUDENT";

    @Column(name = "is_banned", nullable = false)
    private boolean banned = false;

    @Column(name = "league_id")
    private Long leagueId;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @Column(name = "last_lesson_completed_date")
    private java.time.LocalDate lastLessonCompletedDate;

    @Column(name = "hearts", nullable = false)
    private Integer hearts = 5;

    @Column(name = "last_heart_lost_at")
    private java.time.LocalDateTime lastHeartLostAt;

    @Column(name = "total_xp", nullable = false)
    private int totalXp = 0;

    @Column(name = "current_level", nullable = false)
    private int currentLevel = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }
    public Long getLeagueId() { return leagueId; }
    public void setLeagueId(Long leagueId) { this.leagueId = leagueId; }
    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }
    public java.time.LocalDate getLastLessonCompletedDate() { return lastLessonCompletedDate; }
    public void setLastLessonCompletedDate(java.time.LocalDate lastLessonCompletedDate) { this.lastLessonCompletedDate = lastLessonCompletedDate; }
    public Integer getHearts() { return hearts; }
    public void setHearts(Integer hearts) { this.hearts = hearts; }
    public java.time.LocalDateTime getLastHeartLostAt() { return lastHeartLostAt; }
    public void setLastHeartLostAt(java.time.LocalDateTime lastHeartLostAt) { this.lastHeartLostAt = lastHeartLostAt; }
    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
}
