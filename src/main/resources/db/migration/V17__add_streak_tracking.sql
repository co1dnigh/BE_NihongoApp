ALTER TABLE users
  ADD COLUMN last_streak_date  DATE   NULL  AFTER max_energy,
  ADD COLUMN streak_freeze_count INT NOT NULL DEFAULT 0 AFTER last_streak_date;
