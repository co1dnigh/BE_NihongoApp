CREATE TABLE lesson_attempt_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    answered_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES lesson_questions(id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES lesson_question_options(id) ON DELETE CASCADE,
    INDEX idx_lesson_attempt_answers_user_lesson (user_id, lesson_id),
    INDEX idx_lesson_attempt_answers_user_question (user_id, question_id)
);
