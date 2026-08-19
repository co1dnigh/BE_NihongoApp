CREATE TABLE characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    romaji VARCHAR(50) NOT NULL,
    type ENUM('HIRAGANA', 'KATAKANA') NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    audio_url VARCHAR(500),
    stroke_order_data TEXT,
    order_index INT NOT NULL,
    UNIQUE KEY uk_character_type_symbol (type, symbol),
    INDEX idx_characters_type_order (type, group_name, order_index)
);

CREATE TABLE user_character_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    character_id BIGINT NOT NULL,
    mastery_level INT NOT NULL DEFAULT 0,
    last_practiced_at DATETIME,
    CONSTRAINT fk_character_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_character_progress_character FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_character_progress UNIQUE (user_id, character_id),
    CONSTRAINT ck_character_mastery_level CHECK (mastery_level BETWEEN 0 AND 3)
);

ALTER TABLE user_exp_logs
    MODIFY COLUMN source_type ENUM('NEW_LESSON', 'REVIEW_LESSON', 'JUMP_TEST', 'ALPHABET_PRACTICE') NOT NULL;