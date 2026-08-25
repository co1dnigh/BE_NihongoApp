-- Tach node "on tap" tren duong di (bat buoc, cau hoi lay tu topic cu) ra khoi
-- TIMED_REVIEW, dat ten moi la TOPIC_REVIEW. TIMED_REVIEW duoc giu lai de sau nay
-- lam dung nghia "on tap tinh gio": mascot dung ben canh duong di, khong bat buoc,
-- cau hoi cua chinh topic hien tai, co dong ho dem gio that.
--
-- Toan bo lesson dang la TIMED_REVIEW hien nay chinh la loai "on tap topic cu" nen
-- duoc chuyen thang sang TOPIC_REVIEW, khong mat du lieu (giu nguyen orderIndex,
-- progress, stars da dat).

ALTER TABLE lessons
    MODIFY COLUMN lesson_type ENUM('NORMAL', 'TIMED_REVIEW', 'TOPIC_REVIEW', 'JUMP_TEST') NOT NULL;

UPDATE lessons SET lesson_type = 'TOPIC_REVIEW' WHERE lesson_type = 'TIMED_REVIEW';
