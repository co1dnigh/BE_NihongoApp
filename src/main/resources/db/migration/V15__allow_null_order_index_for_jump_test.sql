-- JUMP_TEST không nằm trên đường đi, do đó không có toạ độ hiển thị (visual order)
-- -> Cho phép order_index NULL.
-- Idempotent: chỉ MODIFY khi cột hiện đang NOT NULL.

DROP PROCEDURE IF EXISTS modify_column_if_needed;

DELIMITER $$

CREATE PROCEDURE modify_column_if_needed()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'lessons'
          AND COLUMN_NAME = 'order_index'
          AND IS_NULLABLE = 'NO'
    ) THEN
        ALTER TABLE lessons
            MODIFY COLUMN order_index INT NULL
            COMMENT 'NULL cho JUMP_TEST (vẽ ở Header, không nằm trên đường uốn lượn)';
    END IF;
END$$

DELIMITER ;

CALL modify_column_if_needed();

DROP PROCEDURE modify_column_if_needed;