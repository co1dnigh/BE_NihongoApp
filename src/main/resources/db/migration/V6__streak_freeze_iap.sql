-- =====================================================================================
-- V6__streak_freeze_iap.sql
-- Streak Freeze counter + Bảng staging xác thực IAP (Async/Webhook)
-- =====================================================================================

ALTER TABLE users
    ADD COLUMN streak_freeze_count INT NOT NULL DEFAULT 0,
    ADD COLUMN is_banned BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE iap_receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    platform ENUM('APPLE','GOOGLE') NOT NULL,
    raw_receipt TEXT NOT NULL,
    status ENUM('PENDING','VERIFIED','FAILED') NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_iap_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_iap_status_updated ON iap_receipts(status, updated_at);
