ALTER TABLE users
    ADD COLUMN last_learning_at DATETIME NULL,
    ADD COLUMN last_rank_decay_at DATETIME NULL,
    ADD COLUMN last_rank_reminder_at DATETIME NULL;