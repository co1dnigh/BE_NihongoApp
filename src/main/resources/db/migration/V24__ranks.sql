-- 1. Tạo bảng ranks (Danh mục cấp bậc)
-- Dùng BIGINT để đồng bộ với entity Rank.id và User.rank @ManyToOne(Long-based ID)
CREATE TABLE ranks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    min_exp_required INT NOT NULL,
    order_index INT NOT NULL
);

-- 2. Gieo dữ liệu (Seeding) các mốc hạng cơ bản
INSERT INTO ranks (name, min_exp_required, order_index) VALUES
('BRONZE', 0, 1),
('SILVER', 1000, 2),
('GOLD', 3000, 3),
('PLATINUM', 6000, 4),
('DIAMOND', 10000, 5);

-- 3. Thêm cột rank_id vào bảng users (Tạm thời cho phép NULL để không bị lỗi)
ALTER TABLE users ADD COLUMN rank_id BIGINT AFTER exp;

-- 4. BƯỚC QUAN TRỌNG: Đồng bộ dữ liệu cũ sang hệ thống mới
-- Map chữ 'BRONZE' của người dùng cũ thành ID của hạng BRONZE trong bảng ranks
UPDATE users 
SET rank_id = (SELECT id FROM ranks WHERE name = users.current_league);

-- Phòng hờ trường hợp có user bị trễ/lỗi trước đó, gán mặc định về Đồng (ID = 1)
UPDATE users 
SET rank_id = 1 WHERE rank_id IS NULL;

-- 5. Khóa chặt lại (Thêm FK và Ràng buộc NOT NULL)
ALTER TABLE users MODIFY COLUMN rank_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE users ADD CONSTRAINT fk_users_rank FOREIGN KEY (rank_id) REFERENCES ranks(id);

-- 6. Tạm thời không xóa current_league để giữ tương thích với JPA model hiện tại.
-- Nếu sau này muốn bỏ hoàn toàn cột cũ, thực hiện một migration riêng sau khi app
-- đã được cập nhật để không còn map đến current_league.
-- ALTER TABLE users DROP COLUMN current_league;