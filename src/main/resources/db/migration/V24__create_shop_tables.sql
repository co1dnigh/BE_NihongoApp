-- Shop items (các mặt hàng có thể mua trong shop)
CREATE TABLE shop_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    item_type ENUM('CONSUMABLE', 'COSMETIC', 'POWERUP') NOT NULL,
    effect_type ENUM(
        'STREAK_FREEZE',
        'ENERGY_REFILL',
        'DOUBLE_XP',
        'DOUBLE_COIN',
        'TIMER_BOOST',
        'AVATAR_FRAME',
        'BADGE',
        'THEME'
    ) NOT NULL,
    effect_value INT DEFAULT 0 COMMENT 'Giá trị hiệu ứng (vd: duration phút, multiplier, v.v.)',
    price_coins INT NOT NULL DEFAULT 0,
    price_gems INT DEFAULT 0 COMMENT 'Nếu sau này có gem/premium currency',
    icon_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    limited_time BOOLEAN NOT NULL DEFAULT FALSE,
    available_from DATETIME NULL COMMENT 'Thời gian bắt đầu bán (cho limited-time items)',
    available_until DATETIME NULL COMMENT 'Thời gian hết hạn bán',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
);

-- Mở rộng user_inventories: thêm expires_at (cho powerup), equipped (cho cosmetic), acquired_from, acquired_at
ALTER TABLE user_inventories
    ADD COLUMN expires_at DATETIME NULL COMMENT 'Hết hạn cho powerup (double xp, double coin, timer boost...)',
    ADD COLUMN equipped BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Đang trang bị (cho cosmetic: avatar frame, badge, theme)',
    ADD COLUMN acquired_from ENUM('SHOP_BUY', 'CHEST_REWARD', 'QUEST_REWARD', 'SYSTEM_GIFT', 'EVENT_REWARD') NOT NULL DEFAULT 'SHOP_BUY',
    ADD COLUMN acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Mở rộng inventory_transactions: thêm source_type mới
ALTER TABLE inventory_transactions
    MODIFY COLUMN source_type ENUM('GACHA_DROP', 'STORE_BUY', 'SYSTEM_GIFT', 'CONSUMED', 'QUEST_REWARD', 'CHEST_REWARD', 'EVENT_REWARD') NOT NULL;

-- Seed data cho shop items
INSERT INTO shop_items (name, description, item_type, effect_type, effect_value, price_coins, icon_url, sort_order, active) VALUES
-- Consumables (tiêu hao ngay lập tức)
('Streak Freeze', 'Bảo vệ chuỗi ngày học khi bạn nghỉ 1 ngày', 'CONSUMABLE', 'STREAK_FREEZE', 1, 200, '/icons/streak_freeze.png', 1, TRUE),
('Energy Refill', 'Hồi đầy năng lượng ngay lập tức', 'CONSUMABLE', 'ENERGY_REFILL', 25, 400, '/icons/energy_refill.png', 2, TRUE),

-- Powerups (có thời hạn, tác động gameplay)
('Double XP Boost (30 phút)', 'Nhận gấp đôi EXP trong 30 phút', 'POWERUP', 'DOUBLE_XP', 30, 150, '/icons/double_xp.png', 3, TRUE),
('Double Coin Boost (30 phút)', 'Nhận gấp đôi Coin trong 30 phút', 'POWERUP', 'DOUBLE_COIN', 30, 200, '/icons/double_coin.png', 4, TRUE),
('Timer Boost (10 phút)', 'Tăng thời gian làm bài Timed Review thêm 50% trong 10 phút', 'POWERUP', 'TIMER_BOOST', 10, 100, '/icons/timer_boost.png', 5, TRUE),

-- Cosmetics (chỉ hiển thị, không ảnh hưởng gameplay)
('Khung avatar: Sakura', 'Khung avatar chủ đề hoa anh đào', 'COSMETIC', 'AVATAR_FRAME', 0, 500, '/icons/frame_sakura.png', 10, TRUE),
('Khung avatar: Fuji', 'Khung avatar núi Phú Sĩ', 'COSMETIC', 'AVATAR_FRAME', 0, 500, '/icons/frame_fuji.png', 11, TRUE),
('Khung avatar: Neon Tokyo', 'Khung avatar đèn neon Tokyo', 'COSMETIC', 'AVATAR_FRAME', 0, 800, '/icons/frame_neon.png', 12, TRUE),
('Huy hiệu: Samurai', 'Huy hiệu hiển thị trên hồ sơ', 'COSMETIC', 'BADGE', 0, 300, '/icons/badge_samurai.png', 13, TRUE),
('Huy hiệu: Ninja', 'Huy hiệu hiển thị trên hồ sơ', 'COSMETIC', 'BADGE', 0, 300, '/icons/badge_ninja.png', 14, TRUE),
('Chủ đề: Dark Mode', 'Giao diện tối cho ứng dụng', 'COSMETIC', 'THEME', 0, 1000, '/icons/theme_dark.png', 15, TRUE),
('Chủ đề: Sakura Pink', 'Giao diện hồng hoa anh đào', 'COSMETIC', 'THEME', 0, 1000, '/icons/theme_sakura.png', 16, TRUE);

-- Indexes
CREATE INDEX idx_shop_items_active_type ON shop_items(active, item_type, sort_order);
CREATE INDEX idx_user_inventories_user_equipped ON user_inventories(user_id, equipped);
CREATE INDEX idx_user_inventories_expires ON user_inventories(expires_at);