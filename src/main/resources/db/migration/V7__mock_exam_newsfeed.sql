-- =====================================================================================
-- V7__mock_exam_newsfeed.sql
-- Phân hệ Đề thi thử JLPT + Bảng tin Cộng đồng
-- =====================================================================================

CREATE TABLE mock_exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level ENUM('N5','N4') NOT NULL,
    title VARCHAR(255) NOT NULL,
    time_limit_minutes INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exam_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    section_type ENUM('VOCAB_KANJI','GRAMMAR_READING','LISTENING') NOT NULL,
    pass_threshold INT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_examsection_exam FOREIGN KEY (exam_id) REFERENCES mock_exams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exam_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    question_text TEXT,
    audio_url VARCHAR(500) NULL,
    options_json JSON NOT NULL,
    correct_answer VARCHAR(500) NOT NULL,
    score_value INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_examq_section FOREIGN KEY (section_id) REFERENCES exam_sections(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_examq_section ON exam_questions(section_id);

CREATE TABLE user_exam_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exam_id BIGINT NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP NULL,
    total_score INT NULL,
    is_passed BOOLEAN NULL,
    CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_exam FOREIGN KEY (exam_id) REFERENCES mock_exams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_attempt_user ON user_exam_attempts(user_id);

CREATE TABLE user_exam_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    exam_question_id BIGINT NOT NULL,
    user_answer VARCHAR(500),
    is_correct BOOLEAN,
    CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES user_exam_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_question FOREIGN KEY (exam_question_id) REFERENCES exam_questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE news_feed_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_type ENUM('STREAK_MILESTONE','LEVEL_UP','GACHA_SSR','EXAM_PASSED','LEAGUE_PROMOTION') NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_newsfeed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_newsfeed_created ON news_feed_posts(created_at DESC);
