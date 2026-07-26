-- Thêm cột metadata_json vào bảng lesson_questions và lesson_question_options.
-- Migration idempotent: an toàn khi chạy lại.
-- Mục đích: lưu các metadata mở rộng (romaji, furigana, gợi ý, ...) dưới dạng JSON.

DROP PROCEDURE IF EXISTS add_column_if_not_exists;

DELIMITER $$

CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CALL add_column_if_not_exists(
    'lesson_questions',
    'metadata_json',
    'metadata_json JSON NULL COMMENT ''Lưu romaji, furigana, gợi ý...'' AFTER image_url'
);

CALL add_column_if_not_exists(
    'lesson_question_options',
    'metadata_json',
    'metadata_json JSON NULL COMMENT ''Lưu romaji, furigana của từng block...'' AFTER order_index'
);

DROP PROCEDURE add_column_if_not_exists;