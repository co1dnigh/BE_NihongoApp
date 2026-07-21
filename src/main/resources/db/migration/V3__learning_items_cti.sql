-- =====================================================================================
-- V3__learning_items_cti.sql
-- Gộp vocabulary / grammar_points / kanji dưới learning_items (Class Table Inheritance)
-- =====================================================================================

CREATE TABLE learning_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_type ENUM('VOCAB','GRAMMAR','kanji') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_learning_items_type ON learning_items(item_type);

ALTER TABLE vocabulary ADD COLUMN learning_item_id BIGINT NULL AFTER id;
ALTER TABLE grammar_points ADD COLUMN learning_item_id BIGINT NULL AFTER id;
ALTER TABLE kanji ADD COLUMN learning_item_id BIGINT NULL AFTER id;

ALTER TABLE vocabulary
    MODIFY COLUMN learning_item_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_vocab_learning_item FOREIGN KEY (learning_item_id) REFERENCES learning_items(id) ON DELETE CASCADE,
    ADD UNIQUE KEY uq_vocab_learning_item (learning_item_id);

ALTER TABLE grammar_points
    MODIFY COLUMN learning_item_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_grammar_learning_item FOREIGN KEY (learning_item_id) REFERENCES learning_items(id) ON DELETE CASCADE,
    ADD UNIQUE KEY uq_grammar_learning_item (learning_item_id);

ALTER TABLE kanji
    MODIFY COLUMN learning_item_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_kanji_learning_item FOREIGN KEY (learning_item_id) REFERENCES learning_items(id) ON DELETE CASCADE,
    ADD UNIQUE KEY uq_kanji_learning_item (learning_item_id);

ALTER TABLE lesson_vocabularies ADD COLUMN learning_item_id BIGINT NULL AFTER vocabulary_id;

ALTER TABLE lesson_kanjis ADD COLUMN learning_item_id BIGINT NULL AFTER kanji_id;

-- Refactor questions: thay reference_type/reference_id bằng learning_item_id
ALTER TABLE questions ADD COLUMN learning_item_id BIGINT NULL AFTER lesson_id;

-- Refactor user_knowledge_state
ALTER TABLE user_knowledge_state ADD COLUMN learning_item_id BIGINT NULL AFTER user_id;

-- SQL View denormalize cho hot-path read
CREATE OR REPLACE VIEW v_lesson_vocab_questions AS
SELECT
    q.id AS question_id,
    q.lesson_id AS lesson_id,
    q.question_type AS question_type,
    q.question_text AS question_text,
    q.options_json AS options_json,
    q.correct_answer AS correct_answer,
    v.kana, v.kanji, v.meaning_vi, v.audio_url
FROM questions q
JOIN vocabulary v ON q.learning_item_id = v.learning_item_id;
