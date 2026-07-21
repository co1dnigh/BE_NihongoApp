-- =====================================================================================
-- V10_1__outbox_and_shop.sql
-- Outbox events for leaderboard + Shop items table
-- =====================================================================================

-- Outbox events: eventual consistency for leaderboard ZINCRBY
CREATE TABLE outbox_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_type VARCHAR(50) NOT NULL,
  payload JSON NOT NULL,
  status ENUM('pending','done','failed') NOT NULL DEFAULT 'pending',
  retry_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  processed_at TIMESTAMP NULL,
  INDEX idx_outbox_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Shop items: fixed-price purchases with GEM
CREATE TABLE shop_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_name VARCHAR(100) NOT NULL,
  effect_type ENUM('HEART_REFILL','STREAK_FREEZE') NOT NULL,
  effect_value INT NOT NULL,
  price_gemstone INT NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default shop items
INSERT IGNORE INTO shop_items (id, item_name, effect_type, effect_value, price_gemstone) VALUES
  (1, 'Bình hồi Tim', 'HEART_REFILL', 1, 30),
  (2, 'Thẻ đóng băng Streak', 'STREAK_FREEZE', 1, 80);
