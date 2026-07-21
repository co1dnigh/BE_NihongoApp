-- V13__wallet_gem_guarantee.sql
-- Gamification v4 Step 1b: Atomic GEM redemption guarantee
-- CHECK constraint: gem_balance >= 0 (defense-in-depth, complementary to Java locks)
-- Idempotent: skip if constraint already exists

SET @db := DATABASE();
SET @constraint_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_wallet'
      AND CONSTRAINT_NAME = 'chk_wallet_gem_nonneg'
      AND CONSTRAINT_TYPE = 'CHECK');

SET @sql := IF(@constraint_exists = 0,
    'ALTER TABLE user_wallet ADD CONSTRAINT chk_wallet_gem_nonneg CHECK (gem_balance >= 0)',
    'SELECT ''chk_wallet_gem_nonneg already exists'' AS msg');

PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SELECT 'V13 complete' AS status;
