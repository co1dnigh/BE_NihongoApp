CREATE TABLE quest_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    quest_type ENUM('COMPLETE_LESSONS', 'CORRECT_ANSWERS', 'PERFECT_LESSON') NOT NULL,
    target_value INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_daily_quests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quest_definition_id BIGINT NOT NULL,
    quest_date DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    quest_type ENUM('COMPLETE_LESSONS', 'CORRECT_ANSWERS', 'PERFECT_LESSON') NOT NULL,
    target_value INT NOT NULL,
    current_progress INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quest_definition_id) REFERENCES quest_definitions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_quest_date (user_id, quest_definition_id, quest_date)
);

INSERT INTO quest_definitions (title, quest_type, target_value, active) VALUES
    ('Hoan thanh 1 bai hoc', 'COMPLETE_LESSONS', 1, TRUE),
    ('Hoan thanh 2 bai hoc', 'COMPLETE_LESSONS', 2, TRUE),
    ('Tra loi dung 10 cau', 'CORRECT_ANSWERS', 10, TRUE),
    ('Tra loi dung 20 cau', 'CORRECT_ANSWERS', 20, TRUE),
    ('Hoan thanh 1 bai khong sai cau nao', 'PERFECT_LESSON', 1, TRUE);
