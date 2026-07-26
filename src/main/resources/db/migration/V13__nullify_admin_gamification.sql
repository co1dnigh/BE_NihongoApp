-- ==========================================
-- V13: Nullify gamification & energy cols cho admin row
-- ==========================================
-- Lý do: V12__seed_first_admin.sql chèn admin row nhưng KHÔNG liệt kê các cột
-- gamification. Vì V1 + V11 đặt DEFAULT (1, 0, 'BRONZE', 25, ...), MySQL dùng
-- DEFAULT thay vì NULL. Admin không chơi game, đặt NULL cho đúng ngữ nghĩa.
--
-- Tách thành V13 riêng (thay vì sửa V12) vì V12 đã từng được apply với checksum
-- cũ; sửa V12 sẽ gây checksum mismatch. An toàn nhất là INSERT/UPDATE ở migration mới.
--
-- Idempotent: chỉ UPDATE nếu admin row tồn tại.
UPDATE users
SET
    level = NULL,
    exp = NULL,
    current_league = NULL,
    current_energy = NULL,
    max_energy = NULL,
    coins = NULL,
    current_streak = NULL,
    longest_streak = NULL
WHERE email = 'admin@nihongo.app';
