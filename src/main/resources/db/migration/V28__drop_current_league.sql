-- =========================================================
-- V25: Drop legacy current_league column after rank migration
-- =========================================================
-- Lý do:
-- - V24__ranks.sql đã chuyển logic league sang bảng ranks + cột rank_id
-- - current_league là legacy field cũ, không còn được map trong entity
-- - Bước này giúp dọn dẹp schema và tránh mismatch khi Hibernate validate
--
-- Lưu ý:
-- - MySQL 8.4 chấp nhận DROP COLUMN trực tiếp, nhưng nếu column chưa tồn tại thì
--   migration sẽ bị lỗi ở môi trường đã reset/repair. Vì vậy ta kiểm tra trước.

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'current_league'
);

SET @sql = IF(
    @col_exists > 0,
    'ALTER TABLE users DROP COLUMN current_league',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
