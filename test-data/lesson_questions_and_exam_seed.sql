-- =====================================================================
-- TEST DATA SEED - BE_NihongoApp
-- Bao gồm: 6 loại lesson_questions, exam questions (JLPT), alphabet characters
-- Chạy trực tiếp trên MySQL (schema tính đến migration V33).
-- An toàn để chạy nhiều lần trên DB dev/test (chỉ tạo record mới, không đụng data cũ).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. TOPIC + LESSON làm "container" cho lesson_questions
-- ---------------------------------------------------------------------
INSERT INTO topics (title, description, order_index)
VALUES ('Test Data - N5 Cơ bản', 'Topic dùng để chứa dữ liệu test câu hỏi', 99);
SET @topic_id = LAST_INSERT_ID();

INSERT INTO lessons (topic_id, jlpt_level, title, order_index, lesson_type, config_json)
VALUES (@topic_id, 'N5', 'Bài test - Tất cả loại câu hỏi', 1, 'NORMAL', NULL);
SET @lesson_id = LAST_INSERT_ID();

-- ---------------------------------------------------------------------
-- 1. SELECT_IMAGE - chọn hình đúng với từ vựng
-- ---------------------------------------------------------------------
INSERT INTO lesson_questions (lesson_id, question_type, question_text, audio_url, image_url, metadata_json)
VALUES (@lesson_id, 'SELECT_IMAGE', 'Từ nào tương ứng với hình ảnh: 猫 (con mèo)?', NULL, NULL,
        JSON_OBJECT('romaji', 'neko'));
SET @q1 = LAST_INSERT_ID();

INSERT INTO lesson_question_options (question_id, option_text, image_url, audio_url, is_correct, order_index) VALUES
(@q1, '猫 (con mèo)', 'https://example.com/images/cat.png', NULL, TRUE, 0),
(@q1, '犬 (con chó)', 'https://example.com/images/dog.png', NULL, FALSE, 1),
(@q1, '鳥 (con chim)', 'https://example.com/images/bird.png', NULL, FALSE, 2),
(@q1, '魚 (con cá)',  'https://example.com/images/fish.png', NULL, FALSE, 3);

-- ---------------------------------------------------------------------
-- 2. TRANSLATE_TO_JP - dịch câu tiếng Việt sang tiếng Nhật
-- ---------------------------------------------------------------------
INSERT INTO lesson_questions (lesson_id, question_type, question_text, audio_url, image_url, metadata_json)
VALUES (@lesson_id, 'TRANSLATE_TO_JP', 'Dịch câu sau sang tiếng Nhật: "Tôi là học sinh."', NULL, NULL, NULL);
SET @q2 = LAST_INSERT_ID();

INSERT INTO lesson_question_options (question_id, option_text, is_correct, order_index) VALUES
(@q2, '私は学生です。', TRUE, 0),
(@q2, '私は先生です。', FALSE, 1),
(@q2, '彼は学生です。', FALSE, 2),
(@q2, '私は学生ですか。', FALSE, 3);

-- ---------------------------------------------------------------------
-- 3. TRANSLATE_TO_VN - dịch câu tiếng Nhật sang tiếng Việt
-- ---------------------------------------------------------------------
INSERT INTO lesson_questions (lesson_id, question_type, question_text, audio_url, image_url, metadata_json)
VALUES (@lesson_id, 'TRANSLATE_TO_VN', 'Dịch câu sau sang tiếng Việt: "これは本です。"', NULL, NULL, NULL);
SET @q3 = LAST_INSERT_ID();

INSERT INTO lesson_question_options (question_id, option_text, is_correct, order_index) VALUES
(@q3, 'Đây là quyển sách.', TRUE, 0),
(@q3, 'Đây là cây bút.', FALSE, 1),
(@q3, 'Kia là quyển sách.', FALSE, 2),
(@q3, 'Đây không phải là quyển sách.', FALSE, 3);

-- ---------------------------------------------------------------------
-- 4. LISTEN_AND_ARRANGE - nghe rồi sắp xếp từ đúng thứ tự
--    metadata_json lưu thứ tự đúng để FE render lại khi cần
-- ---------------------------------------------------------------------
INSERT INTO lesson_questions (lesson_id, question_type, question_text, audio_url, image_url, metadata_json)
VALUES (@lesson_id, 'LISTEN_AND_ARRANGE', 'Nghe audio và sắp xếp các từ thành câu đúng.',
        'https://example.com/audio/watashi_wa_gakusei_desu.mp3', NULL,
        JSON_OBJECT('correctOrder', JSON_ARRAY(1, 2, 3)));
SET @q4 = LAST_INSERT_ID();

INSERT INTO lesson_question_options (question_id, option_text, is_correct, order_index) VALUES
(@q4, '私は', TRUE, 1),
(@q4, '学生です', TRUE, 2),
(@q4, '。', TRUE, 3);
-- Lưu ý: với LISTEN_AND_ARRANGE, is_correct=TRUE đánh dấu các block thuộc câu đúng,
-- order_index là vị trí đúng trong câu (FE tự xáo trộn khi hiển thị).

-- ---------------------------------------------------------------------
-- 5. LISTEN_AND_SELECT - nghe rồi chọn đáp án đúng
-- ---------------------------------------------------------------------
INSERT INTO lesson_questions (lesson_id, question_type, question_text, audio_url, image_url, metadata_json)
VALUES (@lesson_id, 'LISTEN_AND_SELECT', 'Nghe audio và chọn từ đúng.',
        'https://example.com/audio/ohayou.mp3', NULL, NULL);
SET @q5 = LAST_INSERT_ID();

INSERT INTO lesson_question_options (question_id, option_text, is_correct, order_index) VALUES
(@q5, 'おはよう (chào buổi sáng)', TRUE, 0),
(@q5, 'こんにちは (chào buổi chiều)', FALSE, 1),
(@q5, 'こんばんは (chào buổi tối)', FALSE, 2),
(@q5, 'さようなら (tạm biệt)', FALSE, 3);

-- ---------------------------------------------------------------------
-- 6. SPEAKING - phát âm theo mẫu
--    Lưu ý: service validate ÉP options không rỗng và có >=1 is_correct=TRUE
--    ngay cả với SPEAKING, nên vẫn phải insert option chứa transcript chuẩn.
-- ---------------------------------------------------------------------
INSERT INTO lesson_questions (lesson_id, question_type, question_text, audio_url, image_url, metadata_json)
VALUES (@lesson_id, 'SPEAKING', 'Phát âm câu sau: おはようございます',
        'https://example.com/audio/ohayou_gozaimasu_sample.mp3', NULL,
        JSON_OBJECT('romaji', 'ohayou gozaimasu'));
SET @q6 = LAST_INSERT_ID();

INSERT INTO lesson_question_options (question_id, option_text, is_correct, order_index) VALUES
(@q6, 'おはようございます', TRUE, 0);

-- =====================================================================
-- 7. EXAM QUESTIONS (JLPT) - bảng questions, không có REST API tạo,
--    chỉ có thể seed bằng SQL.
-- =====================================================================
INSERT INTO exams (title, jlpt_level, total_time_minutes)
VALUES ('Đề test N5 - Dữ liệu mẫu', 'N5', 60);
SET @exam_id = LAST_INSERT_ID();

INSERT INTO exam_sections (exam_id, section_name, time_limit)
VALUES (@exam_id, 'Đọc hiểu (Reading)', 20);
SET @section_reading = LAST_INSERT_ID();

INSERT INTO exam_sections (exam_id, section_name, time_limit)
VALUES (@exam_id, 'Nghe hiểu (Listening)', 20);
SET @section_listening = LAST_INSERT_ID();

-- Câu hỏi đọc hiểu có đoạn văn (passage_text)
INSERT INTO questions (section_id, passage_text, audio_url, question_text, options_json, correct_option_key, points)
VALUES (
  @section_reading,
  '田中さんは毎朝七時に起きます。それから朝ご飯を食べて、学校に行きます。',
  NULL,
  '田中さんは何時に起きますか。',
  JSON_OBJECT('A', '六時', 'B', '七時', 'C', '八時', 'D', '九時'),
  'B',
  2
);

-- Câu hỏi ngữ pháp/từ vựng thường (không có passage/audio)
INSERT INTO questions (section_id, passage_text, audio_url, question_text, options_json, correct_option_key, points)
VALUES (
  @section_reading,
  NULL,
  NULL,
  'これ（　）本です。',
  JSON_OBJECT('A', 'は', 'B', 'を', 'C', 'に', 'D', 'で'),
  'A',
  1
);

-- Câu hỏi nghe (audio_url có giá trị)
INSERT INTO questions (section_id, passage_text, audio_url, question_text, options_json, correct_option_key, points)
VALUES (
  @section_listening,
  NULL,
  'https://example.com/audio/exam_listening_01.mp3',
  '女の人は何を買いますか。',
  JSON_OBJECT('A', 'りんご', 'B', 'みかん', 'C', 'バナナ', 'D', 'ぶどう'),
  'C',
  2
);

-- =====================================================================
-- 8. ALPHABET CHARACTERS (Hiragana + Katakana) - bảng characters
-- =====================================================================
INSERT INTO characters (symbol, romaji, type, group_name, audio_url, stroke_order_data, order_index) VALUES
('あ', 'a',  'HIRAGANA', 'a-row', 'https://example.com/audio/hira_a.mp3',  NULL, 1),
('い', 'i',  'HIRAGANA', 'a-row', 'https://example.com/audio/hira_i.mp3',  NULL, 2),
('う', 'u',  'HIRAGANA', 'a-row', 'https://example.com/audio/hira_u.mp3',  NULL, 3),
('え', 'e',  'HIRAGANA', 'a-row', 'https://example.com/audio/hira_e.mp3',  NULL, 4),
('お', 'o',  'HIRAGANA', 'a-row', 'https://example.com/audio/hira_o.mp3',  NULL, 5),
('か', 'ka', 'HIRAGANA', 'ka-row', 'https://example.com/audio/hira_ka.mp3', NULL, 6),
('き', 'ki', 'HIRAGANA', 'ka-row', 'https://example.com/audio/hira_ki.mp3', NULL, 7),
('ア', 'a',  'KATAKANA', 'a-row', 'https://example.com/audio/kata_a.mp3',  NULL, 1),
('イ', 'i',  'KATAKANA', 'a-row', 'https://example.com/audio/kata_i.mp3',  NULL, 2),
('ウ', 'u',  'KATAKANA', 'a-row', 'https://example.com/audio/kata_u.mp3',  NULL, 3);

-- =====================================================================
-- HẾT. Kiểm tra nhanh sau khi chạy:
-- SELECT * FROM lesson_questions WHERE lesson_id = @lesson_id;
-- SELECT * FROM questions WHERE section_id IN (@section_reading, @section_listening);
-- SELECT * FROM characters ORDER BY type, order_index;
-- =====================================================================
