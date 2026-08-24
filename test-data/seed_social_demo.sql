-- =====================================================================
-- TEST DATA SEED - Social Feed (follow + feed + post/like/comment)
-- Chạy trực tiếp trên MySQL (schema tính đến migration V37).
-- Tạo 5 user demo có thể LOGIN được (POST /api/v1/auth/login) để lấy JWT
-- test tay các API follow/feed/post/like/comment qua curl/Postman.
--
-- Mật khẩu chung cho cả 5 user: Demo@123
-- (password_hash bên dưới là hash BCrypt thật của "Demo@123", tạo bằng đúng
-- BCryptPasswordEncoder mà app đang dùng — login được ngay, không cần đổi gì).
--
-- Có UNIQUE constraint trên email/username nên script CHỈ chạy được 1 LẦN
-- cho mỗi DB — chạy lần 2 sẽ báo lỗi duplicate key (không tạo dữ liệu trùng),
-- đây là hành vi mong muốn.
--
-- QUAN TRỌNG: nội dung có tiếng Việt/Nhật — chạy với client charset utf8mb4,
-- nếu không dữ liệu sẽ bị lỗi font (mojibake). Ví dụ:
--   mysql --default-character-set=utf8mb4 -u <user> -p <database_name> < seed_social_demo.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. 5 USER DEMO
-- ---------------------------------------------------------------------
INSERT INTO users (email, username, password_hash, display_name, role, auth_provider, current_streak, longest_streak, coins, exp, level, version, created_at, updated_at)
VALUES ('demo1@nihongo.app', 'demo_yamada', '$2a$10$YOCGmQ5q1hw3OgpiIe09x.O5ZiCTBRp1wEd/NLDseEB1cHVLHXf6S', 'Yamada Taro', 'LEARNER', 'LOCAL', 5, 12, 120, 340, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @u1 = LAST_INSERT_ID();

INSERT INTO users (email, username, password_hash, display_name, role, auth_provider, current_streak, longest_streak, coins, exp, level, version, created_at, updated_at)
VALUES ('demo2@nihongo.app', 'demo_suzuki', '$2a$10$YOCGmQ5q1hw3OgpiIe09x.O5ZiCTBRp1wEd/NLDseEB1cHVLHXf6S', 'Suzuki Hana', 'LEARNER', 'LOCAL', 2, 8, 80, 210, 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @u2 = LAST_INSERT_ID();

INSERT INTO users (email, username, password_hash, display_name, role, auth_provider, current_streak, longest_streak, coins, exp, level, version, created_at, updated_at)
VALUES ('demo3@nihongo.app', 'demo_tanaka', '$2a$10$YOCGmQ5q1hw3OgpiIe09x.O5ZiCTBRp1wEd/NLDseEB1cHVLHXf6S', 'Tanaka Kenji', 'LEARNER', 'LOCAL', 0, 20, 50, 150, 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @u3 = LAST_INSERT_ID();

INSERT INTO users (email, username, password_hash, display_name, role, auth_provider, current_streak, longest_streak, coins, exp, level, version, created_at, updated_at)
VALUES ('demo4@nihongo.app', 'demo_sato', '$2a$10$YOCGmQ5q1hw3OgpiIe09x.O5ZiCTBRp1wEd/NLDseEB1cHVLHXf6S', 'Sato Mei', 'LEARNER', 'LOCAL', 1, 1, 20, 40, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @u4 = LAST_INSERT_ID();

-- User nay CO CHU DICH khong follow ai ca -> dung de test edge case "feed rong"
-- (chi thay bai cua chinh minh, khong thay bai nguoi khac).
INSERT INTO users (email, username, password_hash, display_name, role, auth_provider, current_streak, longest_streak, coins, exp, level, version, created_at, updated_at)
VALUES ('demo5@nihongo.app', 'demo_ito', '$2a$10$YOCGmQ5q1hw3OgpiIe09x.O5ZiCTBRp1wEd/NLDseEB1cHVLHXf6S', 'Ito Sora', 'LEARNER', 'LOCAL', 0, 0, 0, 0, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @u5 = LAST_INSERT_ID();

-- ---------------------------------------------------------------------
-- 1. FOLLOW CHEO GIUA CAC USER
-- ---------------------------------------------------------------------
-- Yamada (u1) follow Suzuki (u2), Tanaka (u3)
-- Suzuki (u2) follow Yamada (u1), Tanaka (u3), Sato (u4)
-- Tanaka (u3) follow Yamada (u1)
-- Sato   (u4) follow Suzuki (u2)
-- Yamada (u1) cung follow luon Ito (u5) -> Ito co it nhat 1 follower de test GET /followers,
-- nhung Ito (u5) KHONG follow ai -> feed cua Ito rong (tru bai tu dang, neu co).
INSERT INTO user_follows (follower_id, followed_id, created_at) VALUES
(@u1, @u2, CURRENT_TIMESTAMP),
(@u1, @u3, CURRENT_TIMESTAMP),
(@u1, @u5, CURRENT_TIMESTAMP),
(@u2, @u1, CURRENT_TIMESTAMP),
(@u2, @u3, CURRENT_TIMESTAMP),
(@u2, @u4, CURRENT_TIMESTAMP),
(@u3, @u1, CURRENT_TIMESTAMP),
(@u4, @u2, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------
-- 2. BAI DANG (USER_STATUS) + LIKE + COMMENT MAU
-- ---------------------------------------------------------------------
INSERT INTO posts (user_id, content, post_type, created_at, updated_at)
VALUES (@u1, 'Hôm nay học xong bài Kanji N4 đầu tiên! 頑張ります！', 'USER_STATUS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @p1 = LAST_INSERT_ID();

INSERT INTO posts (user_id, content, post_type, created_at, updated_at)
VALUES (@u2, 'Streak 2 ngày rồi, cố lên bản thân ơi!', 'USER_STATUS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @p2 = LAST_INSERT_ID();

INSERT INTO posts (user_id, content, post_type, created_at, updated_at)
VALUES (@u3, 'Ai có mẹo học Kanji nhanh không, chỉ mình với!', 'USER_STATUS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @p3 = LAST_INSERT_ID();

-- Bai cua Ito (u5) - de test: nguoi khac follow Ito thi phai thay bai nay trong feed cua ho.
INSERT INTO posts (user_id, content, post_type, created_at, updated_at)
VALUES (@u5, 'Mới tham gia app, mọi người follow lại mình nhé!', 'USER_STATUS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @p4 = LAST_INSERT_ID();

-- Like: Suzuki + Tanaka like bai cua Yamada; Yamada like bai cua Suzuki.
INSERT INTO post_likes (user_id, post_id, created_at) VALUES
(@u2, @p1, CURRENT_TIMESTAMP),
(@u3, @p1, CURRENT_TIMESTAMP),
(@u1, @p2, CURRENT_TIMESTAMP);

-- Comment: Yamada tra loi cau hoi cua Tanaka; Suzuki cong hien Yamada.
INSERT INTO post_comments (post_id, user_id, content, created_at, updated_at) VALUES
(@p3, @u1, 'Học theo bộ thủ (radical) sẽ nhớ lâu hơn đó bạn ơi!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(@p1, @u2, 'Chúc mừng nhé, cố lên!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
