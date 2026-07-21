-- V12__drop_unused_gold_columns.sql
-- Gamification v4: GEM-only economy cleanup
-- Must drop CHECK constraint first (MySQL blocks DROP COLUMN if CHECK references it)

-- Reseed testuser GEM balance (idempotent upsert)
INSERT INTO user_wallet (user_id, gem_balance, version)
SELECT id, 500, 0 FROM users WHERE username = 'testuser'
ON DUPLICATE KEY UPDATE gem_balance = VALUES(gem_balance), version = VALUES(version);

-- Drop old CHECK constraint that references gold_balance
SET @db := DATABASE();
SET @ck := (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_wallet'
      AND CONSTRAINT_TYPE = 'CHECK' LIMIT 1);
SET @sql := IF(@ck IS NOT NULL,
    CONCAT('ALTER TABLE user_wallet DROP CHECK ', @ck),
    'SELECT ''no check constraint found'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Drop gold_balance only if exists
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_wallet' AND COLUMN_NAME = 'gold_balance');
SET @sql2 := IF(@col_exists > 0,
    'ALTER TABLE user_wallet DROP COLUMN gold_balance',
    'SELECT ''gold_balance already dropped'' AS msg');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

-- Drop cost_currency only if exists
SET @col2 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'gacha_banners' AND COLUMN_NAME = 'cost_currency');
SET @sql3 := IF(@col2 > 0,
    'ALTER TABLE gacha_banners DROP COLUMN cost_currency',
    'SELECT ''cost_currency already dropped'' AS msg');
PREPARE stmt3 FROM @sql3; EXECUTE stmt3; DEALLOCATE PREPARE stmt3;

SELECT 'V12 complete' AS status;
