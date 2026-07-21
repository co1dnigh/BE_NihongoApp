-- =====================================================================================
-- V4__wallet_ledger_gacha.sql
-- Wallet optimistic locking + Ledger giao dịch + Gacha banner/item + Pity + Inventory
-- =====================================================================================

ALTER TABLE user_wallet ADD COLUMN version INT NOT NULL DEFAULT 0;

CREATE TABLE wallet_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    currency_type ENUM('GOLD','GEM') NOT NULL,
    reason ENUM('GACHA_SPIN','LESSON_REWARD','IAP_TOPUP','ADMIN_ADJUST','STREAK_REWARD') NOT NULL,
    reference_id VARCHAR(100) NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    balance_after BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wtx_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_wtx_idempotency UNIQUE (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_wtx_user_created ON wallet_transactions(user_id, created_at);

CREATE TABLE gacha_banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    cost_currency ENUM('GOLD','GEM') NOT NULL,
    cost_amount INT NOT NULL,
    start_at TIMESTAMP NULL,
    end_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE gacha_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    banner_id BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    rarity ENUM('R','SR','SSR') NOT NULL,
    drop_weight INT NOT NULL,
    CONSTRAINT fk_gacha_items_banner FOREIGN KEY (banner_id) REFERENCES gacha_banners(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_gacha_items_banner ON gacha_items(banner_id);

CREATE TABLE user_gacha_pity (
    user_id BIGINT NOT NULL,
    banner_id BIGINT NOT NULL,
    spin_count_since_last_ssr INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, banner_id),
    CONSTRAINT fk_pity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_pity_banner FOREIGN KEY (banner_id) REFERENCES gacha_banners(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_type ENUM('GACHA_ITEM','STREAK_FREEZE','HEART_REFILL') NOT NULL,
    gacha_item_id BIGINT NULL,
    quantity INT NOT NULL DEFAULT 1,
    acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_gacha_item FOREIGN KEY (gacha_item_id) REFERENCES gacha_items(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_inventory_user ON user_inventory(user_id, item_type);
