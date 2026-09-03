ALTER TABLE vocabulary MODIFY COLUMN item_type enum('VOCAB','KANJI','KANA','PHRASE') NOT NULL DEFAULT 'VOCAB';

-- Giả sử user_id của bạn là 1 và 3 câu trên vừa insert có id là 1000, 1001, 1002
INSERT INTO vocabulary (id, item_type, surface, romaji, meaning_vn) VALUES 
(1000, 'PHRASE', 'おはようございます', 'Ohayou gozaimasu', 'Chào buổi sáng'),
(1001, 'PHRASE', 'ありがとうございます', 'Arigatou gozaimasu', 'Cảm ơn'),
(1002, 'PHRASE', 'すみません', 'Sumimasen', 'Xin lỗi');

INSERT INTO user_vocabulary_progress (user_id, vocabulary_id, repetitions, ease_factor, interval_minutes, next_due_at, total_correct, total_wrong)
VALUES 
(1, 1000, 0, 2.5, 0, NOW(), 0, 0),
(1, 1001, 0, 2.5, 0, NOW(), 0, 0),
(1, 1002, 0, 2.5, 0, NOW(), 0, 0);
