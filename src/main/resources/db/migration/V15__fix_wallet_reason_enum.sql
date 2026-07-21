-- =====================================================================================
-- V15__fix_wallet_reason_enum.sql
-- Fix: IAP_TOPUP (legacy migration name) → SHOP_PURCHASE (Java enum value)
-- =====================================================================================
ALTER TABLE wallet_transactions
  MODIFY COLUMN reason ENUM('GACHA_SPIN','LESSON_REWARD','SHOP_PURCHASE','ADMIN_ADJUST','STREAK_REWARD') NOT NULL;
