-- Dem so phien on loi sai da duoc thuong nang luong trong ngay (toi da
-- app.mistake-review.rewarded-sessions-per-day). Reset ve 0 khi sang ngay moi
-- (xu ly o tang service, khong can job rieng - xem MistakeServiceImpl).
ALTER TABLE users
    ADD COLUMN last_mistake_review_reward_date DATE NULL,
    ADD COLUMN mistake_review_reward_count_today INT NOT NULL DEFAULT 0;
