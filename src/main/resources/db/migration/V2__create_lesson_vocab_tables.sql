CREATE TABLE lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    jlpt_level ENUM('N5', 'N4', 'N3', 'N2', 'N1') NOT NULL,
    title VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
);

CREATE TABLE vocabularies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    kanji VARCHAR(100),
    furigana VARCHAR(100),
    romaji VARCHAR(100),
    meaning_vn VARCHAR(255) NOT NULL,
    example_sentence TEXT, -- Câu ví dụ tiếng Nhật
    example_meaning TEXT,  -- Nghĩa của câu ví dụ
    audio_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

CREATE TABLE exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    exercise_type ENUM('MULTIPLE_CHOICE', 'TRANSLATION', 'LISTENING', 'MATCHING') NOT NULL,
    question_text TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    options_json JSON COMMENT 'Lưu trữ linh hoạt các lựa chọn',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);