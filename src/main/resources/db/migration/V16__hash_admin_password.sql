-- ==========================================
-- Hash lại password admin bằng BCrypt
-- ==========================================
-- Password: admin123
-- BCrypt hash (strength=10): generated & verified bằng Python bcrypt
-- Đây là hash MỚI đã được verify: matches("admin123", hash) = true

UPDATE users
SET password_hash = '$2a$10$eUhndaTpURwO2Q0lEMsdNO6s6rW/KRthXfQYpbpte6iJckn5Bi9v2'
WHERE email = 'admin@nihongo.app' AND role = 'ADMIN';
