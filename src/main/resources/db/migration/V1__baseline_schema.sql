-- =====================================================================================
-- V1__baseline_schema.sql
-- Các bảng cốt lõi — Users, Course structure, Content, Wallet cơ bản
-- =====================================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE leagues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    tier_order INT NOT NULL UNIQUE,
    promotion_count INT NOT NULL DEFAULT 10,
    demotion_count INT NOT NULL DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    league_id BIGINT NULL,
    current_streak INT NOT NULL DEFAULT 0,
    last_lesson_completed_date DATE NULL,
    hearts INT NOT NULL DEFAULT 5,
    last_heart_lost_at TIMESTAMP NULL,
    fcm_token VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_league FOREIGN KEY (league_id) REFERENCES leagues(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_users_league ON users(league_id);

CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level ENUM('N5','N4') NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_units_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_units_course ON units(course_id);

CREATE TABLE lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lessons_unit FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_lessons_unit ON lessons(unit_id);

CREATE TABLE vocabulary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kana VARCHAR(100) NOT NULL,
    kanji VARCHAR(100) NULL,
    meaning_vi VARCHAR(255) NOT NULL,
    audio_url VARCHAR(500),
    jlpt_level ENUM('N5','N4') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE grammar_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern VARCHAR(255) NOT NULL,
    explanation_vi TEXT NOT NULL,
    example_sentence TEXT,
    jlpt_level ENUM('N5','N4') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE kanji (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kanji_char VARCHAR(4) NOT NULL,
    onyomi VARCHAR(100),
    kunyomi VARCHAR(100),
    meaning_vi VARCHAR(255) NOT NULL,
    stroke_count INT,
    jlpt_level ENUM('N5','N4') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_id BIGINT NULL,
    reference_type ENUM('VOCAB','GRAMMAR','kanji') NOT NULL,
    reference_id BIGINT NOT NULL,
    question_type ENUM('MULTIPLE_CHOICE','FILL_BLANK','MATCHING') NOT NULL,
    question_text TEXT,
    options_json JSON NULL,
    correct_answer VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_questions_lesson ON questions(lesson_id);
CREATE INDEX idx_questions_reference ON questions(reference_type, reference_id);

CREATE TABLE user_knowledge_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reference_type ENUM('VOCAB','GRAMMAR','kanji') NOT NULL,
    reference_id BIGINT NOT NULL,
    last_reviewed_at TIMESTAMP NULL,
    CONSTRAINT fk_uks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_reference (user_id, reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_mistake_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    user_answer VARCHAR(500),
    is_correct BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_umh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_umh_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_umh_user ON user_mistake_history(user_id);

CREATE TABLE user_wallet (
    user_id BIGINT PRIMARY KEY,
    gold_balance BIGINT NOT NULL DEFAULT 0,
    gem_balance BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_wallet_nonneg CHECK (gold_balance >= 0 AND gem_balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ai_chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aichat_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
