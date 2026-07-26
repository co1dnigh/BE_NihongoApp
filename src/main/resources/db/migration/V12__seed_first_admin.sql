-- ==========================================
-- Seed Admin đầu tiên (Database Seeding)
-- ==========================================
-- Logic: Chèn trực tiếp 1 row vào bảng users với role = 'ADMIN'.
-- Lưu ý: password_hash đang để PLAIN TEXT (không mã hóa) theo logic hiện tại của app
--        (AuthServiceImpl cũng lưu raw password). Sẽ refactor sau khi có BCrypt.
--
-- Admin KHÔNG chơi game, nên KHÔNG liệt kê các cột gamification trong INSERT
-- → DB sẽ lưu NULL cho level, exp, current_league, current_energy, max_energy,
--   coins, current_streak, longest_streak.

INSERT INTO users (
    email,
    username,
    password_hash,
    display_name,
    role,
    auth_provider,
    version,
    created_at,
    updated_at
) VALUES (
    'admin@nihongo.app',
    'admin',
    'admin123',
    'System Admin',
    'ADMIN',
    'LOCAL',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
