-- Kho từ vựng dùng chung + trạng thái ôn tập ngắt quãng (SM-2).
--
-- Ba tính năng cùng đọc một nguồn:
--   1. Bấm giữ vào chữ Nhật bất kỳ để xem nghĩa  -> vocabulary
--   2. Sổ tay "từ đã học"                        -> user_vocabulary_progress.first_learned_at
--   3. Ôn lại đúng lúc sắp quên                  -> user_vocabulary_progress.next_due_at
--
-- V10 từng DROP các bảng `vocabularies` / `user_vocab_reviews` / `user_vocab_review_logs`.
-- Đây là bản dựng lại, khác ở chỗ tách hẳn "từ" khỏi "câu hỏi": trước đây mọi thứ
-- bám vào question_id nên cùng một từ xuất hiện ở 5 câu là 5 thực thể rời rạc,
-- không cộng dồn được trí nhớ và người học chỉ học vẹt đáp án.

-- ==========================================================================
-- 1. KHO TỪ
-- ==========================================================================
CREATE TABLE vocabulary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_type ENUM('VOCAB','KANJI','KANA') NOT NULL DEFAULT 'VOCAB',
    -- utf8mb4_bin, KHÔNG dùng collation mặc định của schema: utf8mb4_unicode_ci
    -- coi hiragana bằng katakana ('おちゃ' = 'オチャ' -> TRUE) và bỏ qua dakuten
    -- ('か' = 'が'). Cả hai đều làm hỏng unique key lẫn phép JOIN theo mặt chữ.
    -- V36 đã vấp đúng lỗi này với `characters.symbol`.
    surface    VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL
               COMMENT 'Mặt chữ hiển thị: おちゃ / 先生',
    reading    VARCHAR(100) NULL     COMMENT 'Cách đọc kana khi surface có kanji',
    romaji     VARCHAR(100) NULL,
    meaning_vn VARCHAR(255) NOT NULL,
    jlpt_level ENUM('N5','N4','N3','N2','N1') NULL,
    audio_url  VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_vocabulary_surface (item_type, surface),
    INDEX idx_vocabulary_surface (surface)
);

-- ==========================================================================
-- 2. NỐI CÂU HỎI <-> TỪ
--    Vừa để sinh glossary cho FE, vừa để biết câu nào ôn được từ nào.
-- ==========================================================================
CREATE TABLE question_vocabulary (
    question_id   BIGINT NOT NULL,
    vocabulary_id BIGINT NOT NULL,
    is_target BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'TRUE = từ trọng tâm của câu (trả lời đúng thì tính là ôn được từ này); FALSE = từ phụ trợ chỉ để tra nghĩa',
    PRIMARY KEY (question_id, vocabulary_id),
    CONSTRAINT fk_qv_question   FOREIGN KEY (question_id)   REFERENCES lesson_questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_qv_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id)       ON DELETE CASCADE,
    INDEX idx_qv_vocabulary (vocabulary_id)
);

-- ==========================================================================
-- 3. TRẠNG THÁI ÔN TẬP THEO NGƯỜI HỌC (SM-2)
-- ==========================================================================
CREATE TABLE user_vocabulary_progress (
    user_id       BIGINT NOT NULL,
    vocabulary_id BIGINT NOT NULL,

    -- Ba số của SM-2.
    repetitions INT NOT NULL DEFAULT 0 COMMENT 'Số lần trả lời đúng liên tiếp',
    ease_factor DECIMAL(4,3) NOT NULL DEFAULT 2.500 COMMENT 'Hệ số dễ nhớ, sàn 1.300',

    -- Đơn vị PHÚT chứ không phải ngày: từ mới cần gặp lại ngay trong cùng phiên
    -- học (10 phút), để đến mai là quá muộn. Anki gọi đây là "learning steps".
    interval_minutes INT NOT NULL DEFAULT 0,

    last_reviewed_at DATETIME NULL,
    next_due_at      DATETIME NULL COMMENT 'NULL = chưa từng học, không nằm trong hàng đợi ôn',
    first_learned_at DATETIME NULL COMMENT 'Lần đầu gặp — dùng cho sổ tay "từ đã học" và nhãn TỪ VỰNG MỚI',

    total_correct INT NOT NULL DEFAULT 0,
    total_wrong   INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, vocabulary_id),
    CONSTRAINT fk_uvp_user       FOREIGN KEY (user_id)       REFERENCES users(id)      ON DELETE CASCADE,
    CONSTRAINT fk_uvp_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE,
    -- Truy vấn nóng nhất: "từ nào của user này đã tới hạn ôn?"
    INDEX idx_uvp_due (user_id, next_due_at)
);

-- ==========================================================================
-- 4. SEED KHO TỪ TỪ `metadata_json.glossary` CÓ SẴN
--
--    1592/1592 câu hỏi đã có sẵn bảng glossary do người soạn bài nhập, tổng
--    5307 cặp (câu, từ) rút gọn còn 465 từ duy nhất. Không phải nhập tay lại.
--
--    MIN() để chốt một nghĩa duy nhất: đúng 2 từ trong toàn bộ dữ liệu có nghĩa
--    ghi lệch nhau giữa các câu, lấy bản nào cũng được miễn là tất định.
-- ==========================================================================
INSERT INTO vocabulary (item_type, surface, romaji, meaning_vn)
SELECT
    'VOCAB',
    g.surface,
    MIN(g.romaji),
    MIN(g.meaning_vn)
FROM (
    SELECT
        jt.surface AS surface,
        JSON_UNQUOTE(JSON_EXTRACT(q.metadata_json, CONCAT('$.glossary."', jt.surface, '".r'))) AS romaji,
        JSON_UNQUOTE(JSON_EXTRACT(q.metadata_json, CONCAT('$.glossary."', jt.surface, '".v'))) AS meaning_vn
    FROM lesson_questions q
    JOIN JSON_TABLE(
        JSON_KEYS(q.metadata_json, '$.glossary'),
        '$[*]' COLUMNS (surface VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin PATH '$')
    ) jt
    WHERE q.metadata_json IS NOT NULL
) g
WHERE g.surface IS NOT NULL
  AND g.surface <> ''
  AND g.meaning_vn IS NOT NULL
  AND g.meaning_vn <> ''
GROUP BY g.surface;

-- ==========================================================================
-- 5. NỐI CÂU HỎI VỚI TỪ
--
--    `is_target` = từ này CHÍNH LÀ nội dung câu hỏi đang kiểm tra (metadata.kana),
--    phân biệt với các từ chỉ góp mặt trong glossary để tra nghĩa. Chỉ từ trọng
--    tâm mới được cập nhật lịch ôn khi trả lời — trả lời đúng một câu không có
--    nghĩa là đã nhớ mọi từ phụ xuất hiện quanh nó.
-- ==========================================================================
INSERT INTO question_vocabulary (question_id, vocabulary_id, is_target)
SELECT
    q.id,
    v.id,
    -- `= NULL` cho ra NULL chứ không phải FALSE, nên phải bọc CASE tường minh.
    CASE
        -- Câu mức TỪ: `kana` chính là từ đang được kiểm tra.
        WHEN JSON_UNQUOTE(JSON_EXTRACT(q.metadata_json, '$.kana')) COLLATE utf8mb4_bin = jt.surface THEN TRUE
        -- Câu mức CÂU: mọi từ thật sự XUẤT HIỆN trong câu đều đang được luyện.
        -- Glossary của câu có thể chứa cả từ không nằm trong câu (soạn dư), nên
        -- phải soi bằng LOCATE thay vì nhận bừa cả bảng.
        WHEN JSON_EXTRACT(q.metadata_json, '$.jp') IS NOT NULL
             AND LOCATE(jt.surface, JSON_UNQUOTE(JSON_EXTRACT(q.metadata_json, '$.jp')) COLLATE utf8mb4_bin) > 0 THEN TRUE
        ELSE FALSE
    END
FROM lesson_questions q
JOIN JSON_TABLE(
    JSON_KEYS(q.metadata_json, '$.glossary'),
    '$[*]' COLUMNS (surface VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin PATH '$')
) jt
JOIN vocabulary v
  ON v.surface = jt.surface AND v.item_type = 'VOCAB'
WHERE q.metadata_json IS NOT NULL;

-- ==========================================================================
-- 6. SEED KANA TỪ BẢNG `characters` ĐÃ CÓ (V34)
--
--    Bảng chữ cái vốn có romaji sẵn; đưa vào cùng kho để tra từ và ôn tập chạy
--    trên một cơ chế duy nhất thay vì `user_character_progress.mastery_level`
--    là một hệ riêng lẻ.
-- ==========================================================================
INSERT IGNORE INTO vocabulary (item_type, surface, romaji, meaning_vn, audio_url)
SELECT 'KANA', c.symbol, c.romaji, CONCAT('Chữ ', c.type, ' đọc là "', c.romaji, '"'), c.audio_url
FROM characters c;

-- ==========================================================================
-- 5b. TỪ TRỌNG TÂM CỦA CÂU CHỈ CÓ ÂM THANH
--
--     `LISTEN_AND_SELECT` không có `kana` lẫn `jp` trong metadata (đề bài nằm ở
--     file âm thanh), nên bước 5 không tìm ra từ trọng tâm nào — 305 câu sẽ
--     không bao giờ đẩy được lịch ôn. Với loại này ĐÁP ÁN ĐÚNG chính là từ đang
--     được kiểm tra, nên lấy từ đó.
-- ==========================================================================
UPDATE question_vocabulary qv
JOIN vocabulary v ON v.id = qv.vocabulary_id
JOIN lesson_question_options o
  ON o.question_id = qv.question_id
 AND o.is_correct = TRUE
 AND o.option_text COLLATE utf8mb4_bin = v.surface
SET qv.is_target = TRUE;
