CREATE TABLE user_lesson_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    status ENUM('LOCKED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'LOCKED',
    unlocked_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_lesson (user_id, lesson_id)
);

CREATE TABLE user_daily_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_date (user_id, activity_date) -- Đảm bảo logic tính Streak chuẩn xác
);

CREATE TABLE user_vocab_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vocab_id BIGINT NOT NULL,
    repetition_count INT DEFAULT 0,
    easiness_factor FLOAT DEFAULT 2.5,
    interval_days INT DEFAULT 0,
    next_review_date DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vocab_id) REFERENCES vocabularies(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_vocab (user_id, vocab_id)
);

CREATE TABLE user_vocab_review_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vocab_id BIGINT NOT NULL,
    quality_rating INT NOT NULL CHECK (quality_rating BETWEEN 0 AND 5),
    review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vocab_id) REFERENCES vocabularies(id) ON DELETE CASCADE
);