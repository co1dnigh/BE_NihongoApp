ALTER TABLE users
  ADD COLUMN streak_freeze_awarded BOOLEAN NOT NULL DEFAULT FALSE AFTER streak_freeze_count;
