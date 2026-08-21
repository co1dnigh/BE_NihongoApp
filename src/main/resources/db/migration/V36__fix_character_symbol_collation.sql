-- Sửa collation của cột `characters.symbol`.
--
-- Vấn đề: cột được tạo với collation mặc định của schema là utf8mb4_0900_ai_ci
-- (accent-insensitive). MySQL coi dấu dakuten/handakuten là "dấu phụ", nên:
--     'か' = 'が'  -> TRUE
--     'は' = 'ぱ'  -> TRUE
-- Hậu quả: unique key uk_character_type_symbol (type, symbol) và câu truy vấn
-- findByTypeAndSymbol() trong AlphabetServiceImpl đều coi mọi chữ có dakuten là
-- trùng với chữ gốc. Thêm 'か' xong thì 'が' bị trả về 409 Character already exists.
-- Không thể nạp nổi 74/208 chữ của bảng chữ cái (25 chữ biến âm + 12 âm ghép,
-- nhân đôi cho Hiragana và Katakana).
--
-- Cách sửa: dùng utf8mb4_bin cho cột symbol — so sánh theo đúng từng byte, đây là
-- hành vi duy nhất đúng cho một khoá định danh ký tự. utf8mb4_0900_as_cs cũng phân
-- biệt được dakuten, nhưng vẫn có thể gộp một số biến thể kana khác (chữ nhỏ ゃ/ャ),
-- nên bin an toàn hơn. Unique key sẽ tự được dựng lại theo collation mới.

ALTER TABLE characters
    MODIFY COLUMN symbol VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
