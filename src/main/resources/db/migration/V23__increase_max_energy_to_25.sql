-- Nang tran max_energy tu 5 len 25 theo thiet ke kinh te nang luong moi:
-- start 1 bai tru 10, bai hoan hao hoan +5 (ton rong 5), bai <=2 loi hoan +2 (ton rong 8),
-- hoi tu nhien +5/gio (day sau 5 tieng). Chi nang tran, KHONG cong them current_energy
-- (user se tu hoi day dan qua co che +5/gio da co san).
UPDATE users
SET max_energy = 25
WHERE role = 'LEARNER' AND (max_energy IS NULL OR max_energy < 25);
