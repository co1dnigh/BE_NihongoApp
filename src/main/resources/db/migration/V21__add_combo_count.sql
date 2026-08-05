-- V21: Add combo_count for tracking consecutive correct answers
ALTER TABLE users ADD COLUMN combo_count INT NOT NULL DEFAULT 0 AFTER coins;
