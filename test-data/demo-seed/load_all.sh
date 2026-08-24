#!/usr/bin/env bash
# Nap toan bo du lieu demo vao MySQL (chay tu thu muc goc du an BE_NihongoApp).
#
#   bash test-data/demo-seed/load_all.sh
#
# Yeu cau: docker compose da chay (docker compose up -d).
# Thu tu bat buoc: seed.sql xoa sach noi dung cu nen phai chay TRUOC seed_users.sql
# (tien do hoc cua user tro toi lesson/question sinh ra o buoc do).
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_ROOT_PASSWORD="${DB_ROOT_PASSWORD:-rootchangeme}"
DB_NAME="${DB_NAME:-nihongo_db}"

run() {
    echo "==> $1"
    docker compose exec -T db mysql --default-character-set=utf8mb4 \
        -u root -p"$DB_ROOT_PASSWORD" "$DB_NAME" < "$DIR/out/$1"
}

run seed.sql            # 12 chu de, 95 bai hoc, ~1.6k cau hoi, ~6k dap an
run seed_kana_audio.sql # am thanh cho ca 208 ky tu bang chu cai
run seed_shop_icons.sql # icon cua hang
run seed_users.sql      # 28 hoc vien demo + tien do + loi sai + tui do

echo
docker compose exec -T db mysql --default-character-set=utf8mb4 -u root -p"$DB_ROOT_PASSWORD" "$DB_NAME" -e "
SELECT 'chu de' muc, COUNT(*) so_luong FROM topics UNION ALL
SELECT 'bai hoc', COUNT(*) FROM lessons UNION ALL
SELECT 'cau hoi', COUNT(*) FROM lesson_questions UNION ALL
SELECT 'dap an', COUNT(*) FROM lesson_question_options UNION ALL
SELECT 'ky tu co am thanh', COUNT(*) FROM characters WHERE audio_url IS NOT NULL UNION ALL
SELECT 'hoc vien', COUNT(*) FROM users WHERE role='LEARNER' UNION ALL
SELECT 'tien do bai hoc', COUNT(*) FROM user_lesson_progress UNION ALL
SELECT 'loi sai da ghi', COUNT(*) FROM user_mistakes;"
echo "XONG."
