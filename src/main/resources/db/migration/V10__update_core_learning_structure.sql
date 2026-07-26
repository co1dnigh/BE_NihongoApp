-- 1. Tạo bảng Chủ đề
CREATE TABLE topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
);

-- 2. Cập nhật bảng Bài học (lessons)
ALTER TABLE lessons
    ADD COLUMN topic_id BIGINT NOT NULL AFTER id,
    ADD COLUMN lesson_type ENUM('NORMAL', 'TIMED_REVIEW', 'JUMP_TEST') DEFAULT 'NORMAL' AFTER order_index,
    ADD COLUMN config_json JSON COMMENT 'Lưu luật chơi riêng: time_limit_sec, entry_cost, star_thresholds' AFTER lesson_type,
    ADD CONSTRAINT fk_lesson_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE;

-- ==========================================
-- 3. DỌN DẸP BẢNG CŨ (Phải drop bảng con trước)
-- ==========================================
DROP TABLE IF EXISTS exercises;
DROP TABLE IF EXISTS user_vocab_review_logs;
DROP TABLE IF EXISTS user_vocab_reviews;
DROP TABLE IF EXISTS vocabularies;

-- 4. Tạo bảng Câu hỏi
CREATE TABLE lesson_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    question_type ENUM(
        'SELECT_IMAGE',
        'TRANSLATE_TO_JP',
        'TRANSLATE_TO_VN',
        'LISTEN_AND_ARRANGE',
        'LISTEN_AND_SELECT',
        'SPEAKING'
    ) NOT NULL,
    question_text TEXT,
    audio_url VARCHAR(255),
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

-- 5. Tạo bảng Đáp án
CREATE TABLE lesson_question_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT,
    image_url VARCHAR(255),
    audio_url VARCHAR(255),
    is_correct BOOLEAN DEFAULT FALSE,
    order_index INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES lesson_questions(id) ON DELETE CASCADE
);

-- ==========================================
-- 6. CẬP NHẬT BẢNG TIẾN ĐỘ ĐÃ CÓ (Thêm cột stars_earned)
-- ==========================================
ALTER TABLE user_lesson_progress
    ADD COLUMN stars_earned INT DEFAULT 0 COMMENT 'Lưu số sao đạt được (0-3) cho màn TIMED_REVIEW' AFTER status;
