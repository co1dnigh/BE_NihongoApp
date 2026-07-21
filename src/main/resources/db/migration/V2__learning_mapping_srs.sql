-- =====================================================================================
-- V2__learning_mapping_srs.sql
-- Bảng ánh xạ nội dung vào Lesson + mở rộng cột SRS cho user_knowledge_state
-- =====================================================================================

CREATE TABLE lesson_vocabularies (
    lesson_id BIGINT NOT NULL,
    vocabulary_id BIGINT NOT NULL,
    PRIMARY KEY (lesson_id, vocabulary_id),
    CONSTRAINT fk_lv_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_lv_vocab FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lesson_kanjis (
    lesson_id BIGINT NOT NULL,
    kanji_id BIGINT NOT NULL,
    PRIMARY KEY (lesson_id, kanji_id),
    CONSTRAINT fk_lk_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_lk_kanji FOREIGN KEY (kanji_id) REFERENCES kanji(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lesson_grammar_points (
    lesson_id BIGINT NOT NULL,
    grammar_point_id BIGINT NOT NULL,
    PRIMARY KEY (lesson_id, grammar_point_id),
    CONSTRAINT fk_lg_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_lg_grammar FOREIGN KEY (grammar_point_id) REFERENCES grammar_points(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE user_knowledge_state
    ADD COLUMN interval_days INT NOT NULL DEFAULT 1 AFTER reference_id,
    ADD COLUMN repetition_count INT NOT NULL DEFAULT 0 AFTER interval_days,
    ADD COLUMN easiness_factor FLOAT NOT NULL DEFAULT 2.5 AFTER repetition_count,
    ADD COLUMN next_review_at TIMESTAMP NULL AFTER easiness_factor;

CREATE INDEX idx_uks_next_review ON user_knowledge_state(user_id, next_review_at);
