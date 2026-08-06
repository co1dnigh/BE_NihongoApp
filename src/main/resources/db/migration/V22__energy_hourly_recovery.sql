-- Doi co che hoi nang luong tu "theo ngay" sang "theo gio" (1 nang luong / 3600 giay).
-- Can doi kieu cot sang DATETIME de tinh duoc so giay troi qua chinh xac (truoc chi co DATE).
ALTER TABLE users
    MODIFY COLUMN last_energy_reset_date DATETIME NULL;

-- Them nguon hoi nang luong moi: xem quang cao (+1 nang luong, cooldown 30 phut).
ALTER TABLE users
    ADD COLUMN last_ad_watch_date DATETIME NULL AFTER last_energy_reset_date;
