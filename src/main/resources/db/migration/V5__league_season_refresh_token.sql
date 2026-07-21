-- =====================================================================================
-- V5__league_season_refresh_token.sql
-- Lịch sử mùa giải Leaderboard + Refresh Token có state + timezone user
-- =====================================================================================

ALTER TABLE users ADD COLUMN timezone_offset_minutes INT NOT NULL DEFAULT 0;

CREATE TABLE league_season_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    league_id BIGINT NOT NULL,
    week_start DATE NOT NULL,
    rank_in_league INT NOT NULL,
    xp_earned INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lsh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_lsh_league FOREIGN KEY (league_id) REFERENCES leagues(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_lsh_user_week ON league_season_history(user_id, week_start);

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_refresh_user ON refresh_tokens(user_id, revoked);
