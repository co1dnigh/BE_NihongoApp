-- Cập nhật bảng Users: Chuyển từ Tim sang Năng lượng và thêm Cấp bậc (League)
ALTER TABLE users
    DROP COLUMN hearts,
    ADD COLUMN current_league ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND') DEFAULT 'BRONZE' AFTER exp,
    ADD COLUMN current_energy INT DEFAULT 25 AFTER current_league,
    ADD COLUMN max_energy INT DEFAULT 25 AFTER current_energy,
    ADD COLUMN last_energy_reset_date DATE AFTER max_energy;

-- Cập nhật bảng gacha_items: Đổi REFILL_HEARTS thành REFILL_ENERGY
ALTER TABLE gacha_items
    MODIFY COLUMN item_type ENUM('FREEZE_STREAK', 'REFILL_ENERGY', 'COSMETIC') NOT NULL;

-- Tạo bảng Log Kinh nghiệm để phục vụ Bảng xếp hạng tuần (Weekly Leaderboard)
CREATE TABLE user_exp_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exp_gained INT NOT NULL,
    source_type ENUM('NEW_LESSON', 'REVIEW_LESSON', 'JUMP_TEST') NOT NULL,
    reference_id BIGINT COMMENT 'ID của lesson hoặc topic vừa học',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
