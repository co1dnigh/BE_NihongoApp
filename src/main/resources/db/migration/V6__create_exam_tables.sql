CREATE TABLE exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    jlpt_level ENUM('N5', 'N4', 'N3', 'N2', 'N1') NOT NULL,
    total_time_minutes INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
);

CREATE TABLE exam_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    section_name VARCHAR(100) NOT NULL,
    time_limit INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
);

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    passage_text TEXT,
    audio_url VARCHAR(255),
    question_text TEXT NOT NULL,
    options_json JSON NOT NULL COMMENT 'Lưu danh sách đáp án dạng JSON',
    correct_option_key VARCHAR(50) NOT NULL COMMENT 'Key của đáp án đúng trong JSON',
    points INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (section_id) REFERENCES exam_sections(id) ON DELETE CASCADE
);

CREATE TABLE exam_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exam_id BIGINT NOT NULL,
    total_score INT NOT NULL DEFAULT 0,
    status ENUM('IN_PROGRESS', 'SUBMITTED', 'ABANDONED') DEFAULT 'IN_PROGRESS',
    current_section_id BIGINT,
    started_at DATETIME NOT NULL,
    submitted_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
);

CREATE TABLE user_exam_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_result_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    user_choice_key VARCHAR(50),
    is_correct BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_result_id) REFERENCES exam_results(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_result_question (exam_result_id, question_id)
);