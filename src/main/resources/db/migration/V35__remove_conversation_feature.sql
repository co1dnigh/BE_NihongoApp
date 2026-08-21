-- Gỡ bỏ hoàn toàn tính năng Conversation khỏi schema và dữ liệu.
--
-- Bối cảnh: migration "V33 create conversation tables" (từ một nhánh khác) đã được
-- áp vào DB dev, nhưng code của nhánh này không có giá trị CONVERSATION trong enum
-- `Lesson.LessonType`. Hậu quả: GET /api/v1/topics nạp toàn bộ lesson và ném lỗi
-- khi map bản ghi có lesson_type = 'CONVERSATION' -> HTTP 500 cho mọi user.
--
-- Quyết định: bỏ hẳn tính năng conversation thay vì thêm giá trị enum vào code.

-- Ghi lại các topic đang chứa bài hội thoại trước khi xoá lesson.
CREATE TEMPORARY TABLE tmp_conversation_topics AS
SELECT DISTINCT topic_id FROM lessons WHERE lesson_type = 'CONVERSATION';

-- Bỏ bảng của tính năng conversation (đồng thời gỡ FK trỏ về lessons/topics).
DROP TABLE IF EXISTS conversation_turns;
DROP TABLE IF EXISTS conversation_sessions;

-- Dọn dữ liệu phụ thuộc rồi mới xoá lesson.
DELETE FROM lesson_attempt_answers
WHERE lesson_id IN (SELECT id FROM lessons WHERE lesson_type = 'CONVERSATION');

DELETE FROM user_lesson_progress
WHERE lesson_id IN (SELECT id FROM lessons WHERE lesson_type = 'CONVERSATION');

DELETE FROM lesson_questions
WHERE lesson_id IN (SELECT id FROM lessons WHERE lesson_type = 'CONVERSATION');

DELETE FROM lessons WHERE lesson_type = 'CONVERSATION';

-- Xoá topic chỉ sinh ra để chứa bài hội thoại (giờ đã rỗng). Topic nào còn bài
-- học khác thì giữ nguyên.
DELETE FROM topics
WHERE id IN (SELECT topic_id FROM tmp_conversation_topics)
  AND id NOT IN (SELECT DISTINCT topic_id FROM lessons);

DROP TEMPORARY TABLE tmp_conversation_topics;

-- Trả enum lesson_type về đúng tập giá trị mà code đang hỗ trợ, để không bản ghi
-- CONVERSATION nào lọt vào lại và làm sập /api/v1/topics lần nữa.
ALTER TABLE lessons
    MODIFY COLUMN lesson_type ENUM('NORMAL', 'TIMED_REVIEW', 'JUMP_TEST')
    NOT NULL DEFAULT 'NORMAL';
