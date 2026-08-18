CREATE TABLE user_mistakes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    wrong_count INT NOT NULL DEFAULT 1,
    correct_streak INT NOT NULL DEFAULT 0,
    status ENUM('ACTIVE', 'RESOLVED') NOT NULL DEFAULT 'ACTIVE',
    last_wrong_at DATETIME NOT NULL,
    last_correct_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES lesson_questions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_mistakes_user_question (user_id, question_id)
);
