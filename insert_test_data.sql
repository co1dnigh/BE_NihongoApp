INSERT INTO lesson_questions (id, lesson_id, question_type, question_text) VALUES
  (1, 1, 'SELECT_IMAGE', 'あ = ?'),
  (2, 1, 'SELECT_IMAGE', 'い = ?'),
  (3, 1, 'SELECT_IMAGE', 'う = ?'),
  (4, 1, 'SELECT_IMAGE', 'え = ?'),
  (5, 1, 'SELECT_IMAGE', 'お = ?'),
  (6, 1, 'SELECT_IMAGE', 'か = ?'),
  (7, 1, 'SELECT_IMAGE', 'き = ?');

INSERT INTO lesson_question_options (id, question_id, option_text, is_correct, order_index) VALUES
  (1, 1, 'a', 1, 1), (2, 1, 'i', 0, 2), (3, 1, 'u', 0, 3), (4, 1, 'e', 0, 4),
  (5, 2, 'i', 1, 1), (6, 2, 'a', 0, 2), (7, 2, 'u', 0, 3), (8, 2, 'e', 0, 4),
  (9, 3, 'u', 1, 1), (10, 3, 'a', 0, 2), (11, 3, 'i', 0, 3), (12, 3, 'e', 0, 4),
  (13, 4, 'e', 1, 1), (14, 4, 'a', 0, 2), (15, 4, 'i', 0, 3), (16, 4, 'u', 0, 4),
  (17, 5, 'o', 1, 1), (18, 5, 'a', 0, 2), (19, 5, 'i', 0, 3), (20, 5, 'u', 0, 4),
  (21, 6, 'ka', 1, 1), (22, 6, 'ki', 0, 2), (23, 6, 'ke', 0, 3), (24, 6, 'ko', 0, 4),
  (25, 7, 'ki', 1, 1), (26, 7, 'ka', 0, 2), (27, 7, 'ku', 0, 3), (28, 7, 'ke', 0, 4);
