-- =====================================================================================
-- V10__gamification_v4_cleanup.sql
-- Gamification v4: GEM-only prep, XP/level, lesson completion log
-- =====================================================================================

-- Add XP/level columns to users
ALTER TABLE users
  ADD COLUMN total_xp INT NOT NULL DEFAULT 0 AFTER current_streak,
  ADD COLUMN current_level INT NOT NULL DEFAULT 1 AFTER total_xp;

-- Level config: XP required per level
CREATE TABLE level_config (
  level INT PRIMARY KEY,
  xp_required INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Lesson completion log: idempotency layer 2 (1 completion per user per lesson)
CREATE TABLE lesson_completion_log (
  user_id BIGINT NOT NULL,
  lesson_id BIGINT NOT NULL,
  completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, lesson_id),
  CONSTRAINT fk_lcl_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_lcl_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed level_config: levels 1-10
INSERT IGNORE INTO level_config (level, xp_required) VALUES
  (1, 0),
  (2, 100),
  (3, 250),
  (4, 500),
  (5, 900),
  (6, 1500),
  (7, 2300),
  (8, 3500),
  (9, 5000),
  (10, 7000);
