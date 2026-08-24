# -*- coding: utf-8 -*-
"""
Sinh du lieu nguoi dung demo: out/seed_users.sql

Tao ra mot lop hoc gia lap voi nhieu trang thai khac nhau de demo duoc MOI tinh nang:
  - nguoi moi tinh (chua hoc gi)          -> demo luong onboarding tu dau
  - nguoi dang hoc do                     -> demo lo trinh khoa/mo khoa, ngan hang loi sai
  - nguoi hoc xa, hang cao, streak dai    -> demo rank, leaderboard, cua hang, tui do
  - nhieu hoc vien phu                    -> bang xep hang co du nguoi

Mat khau chung cua moi tai khoan demo: demo1234
"""
import io
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import build  # noqa: E402

random.seed(4649)

BCRYPT_DEMO1234 = "$2a$10$gvk5i5brDoqKcEzNjz5.aubXgVDZ14zIIV9AW7FHghEWM6TyS1Qnm"
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "out", "seed_users.sql")

# id 1 = admin, 8/9 = tài khoản thật của chủ máy -> không đụng vào
FIRST_DEMO_ID = 100

build.build()
S = build.S

# ---- gom bài học theo chủ đề, đúng thứ tự mở khoá của LessonUnlockPolicy ----
topics = sorted(S.topics, key=lambda t: t[3])
lessons_by_topic = {}
for l in S.lessons:
    lessons_by_topic.setdefault(l[1], []).append(l)
for tid in lessons_by_topic:
    lessons_by_topic[tid].sort(key=lambda l: (l[3] if l[3] is not None else 999))

# chuỗi bài theo đúng thứ tự người học đi qua
ordered_lessons = []
for t in topics:
    ordered_lessons.extend(lessons_by_topic[t[0]])

questions_by_lesson = {}
for q in S.questions:
    questions_by_lesson.setdefault(q[1], []).append(q)
options_by_question = {}
for o in S.options:
    options_by_question.setdefault(o[1], []).append(o)

RANKS = [(1, "BRONZE", 0), (2, "SILVER", 1000), (3, "GOLD", 3000),
         (4, "PLATINUM", 6000), (5, "DIAMOND", 10000)]


def rank_of(exp):
    rid = 1
    for i, name, need in RANKS:
        if exp >= need:
            rid = i
    return rid


def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "1" if v else "0"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


# ---------------------------------------------------------------------------
# Danh sách học viên demo
#   done_lessons : số bài NORMAL đầu tiên (tính trên toàn lộ trình) đã hoàn thành
# ---------------------------------------------------------------------------
LEARNERS = [
    dict(id=100, email="moi@nihongo.app", username="hocvienmoi", name="Trần Bảo An",
         exp=0, coins=0, streak=0, longest=0, energy=25, done=0,
         note="Người mới hoàn toàn — dùng để demo luồng học từ con số 0"),
    dict(id=101, email="linh@nihongo.app", username="linhnt", name="Nguyễn Thùy Linh",
         exp=1450, coins=860, streak=12, longest=15, energy=15, done=14,
         note="Đang học dở Katakana — demo lộ trình khoá/mở khoá và ngân hàng lỗi sai"),
    dict(id=102, email="minh@nihongo.app", username="minhtq", name="Trần Quang Minh",
         exp=3850, coins=2470, streak=45, longest=45, energy=25, done=34,
         note="Đã qua bảng chữ cái + chào hỏi + giới thiệu bản thân, hạng Vàng"),
    dict(id=103, email="mai@nihongo.app", username="mainl", name="Lê Ngọc Mai",
         exp=6900, coins=4100, streak=78, longest=80, energy=20, done=52,
         note="Hạng Bạch Kim, streak dài — demo cửa hàng và vật phẩm"),
    dict(id=104, email="hung@nihongo.app", username="hungpv", name="Phạm Việt Hùng",
         exp=12400, coins=7300, streak=126, longest=126, energy=25, done=70,
         note="Hạng Kim Cương, đứng đầu bảng xếp hạng"),
    dict(id=105, email="an@nihongo.app", username="anvt", name="Vũ Tuấn An",
         exp=9200, coins=3050, streak=31, longest=52, energy=10, done=60),
    dict(id=106, email="thao@nihongo.app", username="thaopt", name="Phan Thu Thảo",
         exp=5400, coins=1980, streak=24, longest=30, energy=25, done=45),
    dict(id=107, email="duc@nihongo.app", username="ducnm", name="Ngô Minh Đức",
         exp=2750, coins=1250, streak=8, longest=19, energy=5, done=26),
    dict(id=108, email="ha@nihongo.app", username="hatt", name="Đỗ Thanh Hà",
         exp=1980, coins=640, streak=3, longest=11, energy=25, done=19),
    dict(id=109, email="khanh@nihongo.app", username="khanhbq", name="Bùi Quốc Khánh",
         exp=890, coins=420, streak=5, longest=7, energy=18, done=9),
    dict(id=110, email="trang@nihongo.app", username="trangvt", name="Vũ Thùy Trang",
         exp=430, coins=180, streak=2, longest=4, energy=25, done=5),
    dict(id=111, email="nam@nihongo.app", username="namhd", name="Hoàng Đức Nam",
         exp=180, coins=95, streak=1, longest=3, energy=22, done=2),
    # --- học viên phụ: để bảng xếp hạng của MỌI hạng đều có đủ người ---
    dict(id=112, email="son@nihongo.app", username="sonlt", name="Lý Thành Sơn",
         exp=14800, coins=6100, streak=94, longest=110, energy=25, done=74),
    dict(id=113, email="yen@nihongo.app", username="yendh", name="Đinh Hải Yến",
         exp=11200, coins=5200, streak=61, longest=88, energy=15, done=66),
    dict(id=114, email="tu@nihongo.app", username="tunm", name="Nguyễn Minh Tú",
         exp=10300, coins=4650, streak=40, longest=57, energy=25, done=62),
    dict(id=115, email="quyen@nihongo.app", username="quyennt", name="Nguyễn Thị Quyên",
         exp=8600, coins=3720, streak=27, longest=44, energy=20, done=56),
    dict(id=116, email="dat@nihongo.app", username="dattv", name="Trịnh Văn Đạt",
         exp=7400, coins=2900, streak=18, longest=33, energy=25, done=50),
    dict(id=117, email="chi@nihongo.app", username="chilm", name="Lâm Mỹ Chi",
         exp=6200, coins=2210, streak=14, longest=26, energy=12, done=47),
    dict(id=118, email="hieu@nihongo.app", username="hieutd", name="Tạ Đức Hiếu",
         exp=5100, coins=1860, streak=21, longest=25, energy=25, done=42),
    dict(id=119, email="lan@nihongo.app", username="lannp", name="Ngô Phương Lan",
         exp=4300, coins=1540, streak=9, longest=22, energy=25, done=38),
    dict(id=120, email="bao@nihongo.app", username="baohg", name="Hoàng Gia Bảo",
         exp=3200, coins=1120, streak=6, longest=14, energy=8, done=30),
    dict(id=121, email="nga@nihongo.app", username="ngaptt", name="Phạm Thị Thanh Nga",
         exp=2400, coins=880, streak=4, longest=12, energy=25, done=23),
    dict(id=122, email="phuc@nihongo.app", username="phucnh", name="Nguyễn Hoàng Phúc",
         exp=1650, coins=560, streak=7, longest=9, energy=25, done=16),
    dict(id=123, email="uyen@nihongo.app", username="uyendt", name="Dương Tú Uyên",
         exp=1150, coins=390, streak=2, longest=6, energy=25, done=11),
    dict(id=124, email="long@nihongo.app", username="longvh", name="Vũ Hải Long",
         exp=760, coins=310, streak=3, longest=5, energy=25, done=8),
    dict(id=125, email="tam@nihongo.app", username="tamnt", name="Nguyễn Thị Tâm",
         exp=520, coins=240, streak=1, longest=3, energy=25, done=6),
    dict(id=126, email="kien@nihongo.app", username="kiendt", name="Đặng Trung Kiên",
         exp=290, coins=140, streak=2, longest=2, energy=25, done=3),
    dict(id=127, email="my@nihongo.app", username="myttk", name="Trần Thị Kim Mỹ",
         exp=95, coins=60, streak=1, longest=1, energy=25, done=1),
]

L = []
A = L.append
A("-- ==========================================================")
A("-- Nguoi dung demo cho BE_NihongoApp (sinh tu build_users.py)")
A("-- Mat khau moi tai khoan: demo1234")
A("-- ==========================================================")
A("SET NAMES utf8mb4;")
A("")
A("-- Container MySQL chay gio UTC con ung dung doc/ghi theo Asia/Ho_Chi_Minh")
A("-- (xem serverTimezone trong application.yml). Neu seed dung thang NOW() thi moi moc")
A("-- thoi gian bi lui 7 tieng, khien EnergyService tuong da troi qua 7 gio va hoi day")
A("-- nang luong ngay lan goi API dau tien. Vi vay dung @vn_now = gio Viet Nam.")
A("SET @vn_now := CONVERT_TZ(NOW(), '+00:00', '+07:00');")
A("SET @vn_today := DATE(@vn_now);")
A("")
A("-- Xoa cac tai khoan demo cu (id >= %d) va toan bo trang thai hoc cua chung" % FIRST_DEMO_ID)
A("DELETE FROM users WHERE id >= %d;" % FIRST_DEMO_ID)
A("")
A("-- Cac tai khoan test cu (Test Hang Dong/Bac/Vang) doi thanh hoc vien co ten that")
A("DELETE FROM user_lesson_progress WHERE user_id IN (2,3,4);")
A("DELETE FROM user_mistakes WHERE user_id IN (2,3,4);")
A("DELETE FROM lesson_attempt_answers WHERE user_id IN (2,3,4);")
A("DELETE FROM users WHERE id IN (2,3,4);")
A("")

# ---------------------------------------------------------------- users
A("-- 1. Tai khoan hoc vien")
for u in LEARNERS:
    rid = rank_of(u["exp"])
    # người có streak thì hôm nay coi như đã học rồi, trừ 2 người để demo nhắc nhở streak
    streak_date = "@vn_today" if u["streak"] and u["id"] not in (107, 109) else (
        "DATE_SUB(@vn_today, INTERVAL 1 DAY)" if u["streak"] else "NULL")
    A("INSERT INTO users (id, email, username, password_hash, auth_provider, display_name, "
      "avatar_url, role, level, exp, rank_id, current_energy, max_energy, last_streak_date, "
      "streak_freeze_count, coins, current_streak, longest_streak, last_energy_reset_date) VALUES "
      "(%d, %s, %s, %s, 'LOCAL', %s, %s, 'LEARNER', %d, %d, %d, %d, 25, %s, %d, %d, %d, %d, @vn_now);"
      % (u["id"], q(u["email"]), q(u["username"]), q(BCRYPT_DEMO1234), q(u["name"]),
         q("/uploads/images/avatars/%s.png" % u["username"]),
         max(1, u["exp"] // 300 + 1), u["exp"], rid, u["energy"], streak_date,
         2 if u["exp"] > 3000 else 0, u["coins"], u["streak"], u["longest"]))
A("")

# ---------------------------------------------------------------- progress
A("-- 2. Tien do bai hoc (theo dung thu tu mo khoa cua LessonUnlockPolicy)")
normal_seq = [l for l in ordered_lessons if l[4] == "NORMAL"]
for u in LEARNERS:
    if not u["done"]:
        continue
    done_normal = normal_seq[:u["done"]]
    done_ids = {l[0] for l in done_normal}
    # bài ôn tập tính giờ của chủ đề nào đã học hết bài thường thì cũng coi như đã qua
    for t in topics:
        tl = lessons_by_topic[t[0]]
        norms = [l for l in tl if l[4] == "NORMAL"]
        if norms and all(l[0] in done_ids for l in norms):
            for l in tl:
                if l[4] == "TIMED_REVIEW":
                    done_ids.add(l[0])
    for lid in sorted(done_ids):
        lesson = S.lessons[lid - 1]
        stars = random.choice([2, 3, 3]) if lesson[4] == "TIMED_REVIEW" else 0
        A("INSERT INTO user_lesson_progress (user_id, lesson_id, status, stars_earned, unlocked_at) "
          "VALUES (%d, %d, 'COMPLETED', %d, @vn_now);" % (u["id"], lid, stars))
    # bài kế tiếp đang học dở
    nxt = normal_seq[u["done"]] if u["done"] < len(normal_seq) else None
    if nxt and u["id"] in (101, 103):
        A("INSERT INTO user_lesson_progress (user_id, lesson_id, status, stars_earned, unlocked_at) "
          "VALUES (%d, %d, 'IN_PROGRESS', 0, @vn_now);" % (u["id"], nxt[0]))
A("")

# ---------------------------------------------------------------- lịch sử EXP/coin
A("-- 3. Lich su EXP va Coin (de man hinh thong ke co du lieu that)")
for u in LEARNERS:
    if not u["done"]:
        continue
    done_normal = normal_seq[:u["done"]]
    for i, l in enumerate(done_normal[-12:]):          # 12 bài gần nhất
        exp = 15 if l[1] <= 2 else 20
        A("INSERT INTO user_exp_logs (user_id, exp_gained, source_type, reference_id, created_at) "
          "VALUES (%d, %d, 'NEW_LESSON', %d, DATE_SUB(@vn_now, INTERVAL %d DAY));"
          % (u["id"], exp, l[0], 12 - i))
        A("INSERT INTO coin_transactions (user_id, amount, transaction_type, reference_id, created_at) "
          "VALUES (%d, %d, 'EARN_LESSON', %d, DATE_SUB(@vn_now, INTERVAL %d DAY));"
          % (u["id"], random.choice([8, 10, 12, 15, 20]), l[0], 12 - i))
    if u["streak"] >= 7:
        A("INSERT INTO coin_transactions (user_id, amount, transaction_type, created_at) "
          "VALUES (%d, 50, 'STREAK_BONUS', DATE_SUB(@vn_now, INTERVAL %d DAY));"
          % (u["id"], max(1, u["streak"] - 7)))
    if u["streak"] >= 30:
        A("INSERT INTO coin_transactions (user_id, amount, transaction_type, created_at) "
          "VALUES (%d, 200, 'STREAK_BONUS', DATE_SUB(@vn_now, INTERVAL %d DAY));"
          % (u["id"], max(1, u["streak"] - 30)))
A("")

# ---------------------------------------------------------------- ngân hàng lỗi sai
A("-- 4. Ngan hang loi sai (chi cho nguoi da hoc, lay dung cau hoi trong bai ho da lam)")
for u in LEARNERS:
    if u["done"] < 3:
        continue
    pool = []
    for l in normal_seq[:u["done"]]:
        for qq in questions_by_lesson.get(l[0], []):
            if qq[2] != "LISTEN_AND_ARRANGE" and qq[2] != "SPEAKING":
                pool.append(qq)
    if not pool:
        continue
    n_mistakes = min(len(pool), 4 + u["done"] // 6)
    picked = random.sample(pool, n_mistakes)
    for i, qq in enumerate(picked):
        wrong_opts = [o for o in options_by_question.get(qq[0], []) if not o[5]]
        if not wrong_opts:
            continue
        wrong = random.choice(wrong_opts)
        days = random.randint(0, 9)
        resolved = i % 5 == 4                       # 1/5 số lỗi đã được xoá nợ
        A("INSERT INTO user_mistakes (user_id, question_id, wrong_count, correct_streak, status, "
          "last_wrong_at, last_correct_at) VALUES (%d, %d, %d, %d, '%s', "
          "DATE_SUB(@vn_now, INTERVAL %d DAY), %s);"
          % (u["id"], qq[0], random.randint(1, 3), 2 if resolved else random.choice([0, 0, 1]),
             "RESOLVED" if resolved else "ACTIVE", days,
             "DATE_SUB(@vn_now, INTERVAL %d DAY)" % max(0, days - 2) if resolved else "NULL"))
        A("INSERT INTO lesson_attempt_answers (user_id, lesson_id, question_id, selected_option_id, "
          "is_correct, answered_at) VALUES (%d, %d, %d, %d, 0, DATE_SUB(@vn_now, INTERVAL %d DAY));"
          % (u["id"], qq[1], qq[0], wrong[0], days))
A("")

# ---------------------------------------------------------------- bảng chữ cái
A("-- 5. Tien do bang chu cai (bang characters co san 208 ky tu)")
A("INSERT INTO user_character_progress (user_id, character_id, mastery_level, last_practiced_at)")
A("SELECT 101, c.id, 3, @vn_now FROM characters c WHERE c.type = 'HIRAGANA' LIMIT 46;")
A("INSERT INTO user_character_progress (user_id, character_id, mastery_level, last_practiced_at)")
A("SELECT 101, c.id, 1, @vn_now FROM characters c WHERE c.type = 'KATAKANA' LIMIT 15;")
for uid in (102, 103, 104):
    A("INSERT INTO user_character_progress (user_id, character_id, mastery_level, last_practiced_at)")
    A("SELECT %d, c.id, 3, @vn_now FROM characters c LIMIT 104;" % uid)
A("")

# ---------------------------------------------------------------- túi đồ
A("-- 6. Tui do (mua tu cua hang) + powerup dang co hieu luc")
A("INSERT INTO user_inventories (user_id, item_id, quantity, acquired_from, equipped) "
  "SELECT 103, id, 2, 'SHOP_BUY', 0 FROM shop_items WHERE effect_type = 'STREAK_FREEZE';")
A("INSERT INTO user_inventories (user_id, item_id, quantity, acquired_from, equipped) "
  "SELECT 103, id, 1, 'SHOP_BUY', 1 FROM shop_items WHERE effect_type = 'AVATAR_FRAME' LIMIT 1;")
A("INSERT INTO user_inventories (user_id, item_id, quantity, acquired_from, equipped) "
  "SELECT 104, id, 1, 'SHOP_BUY', 1 FROM shop_items WHERE effect_type = 'BADGE' LIMIT 1;")
A("INSERT INTO user_inventories (user_id, item_id, quantity, acquired_from, equipped) "
  "SELECT 104, id, 3, 'SHOP_BUY', 0 FROM shop_items WHERE effect_type = 'ENERGY_REFILL';")
A("INSERT INTO user_inventories (user_id, item_id, quantity, acquired_from, equipped) "
  "SELECT 102, id, 1, 'SHOP_BUY', 0 FROM shop_items WHERE effect_type = 'DOUBLE_XP';")
A("INSERT INTO coin_transactions (user_id, amount, transaction_type, created_at) VALUES "
  "(103, -500, 'SHOP_PURCHASE', DATE_SUB(@vn_now, INTERVAL 3 DAY)),"
  "(104, -300, 'SHOP_PURCHASE', DATE_SUB(@vn_now, INTERVAL 5 DAY)),"
  "(102, -150, 'SHOP_PURCHASE', DATE_SUB(@vn_now, INTERVAL 1 DAY));")
A("")
A("-- Powerup Double XP dang chay cua Minh (con hieu luc 25 phut nua)")
A("INSERT INTO user_active_effects (user_id, effect_type, expires_at, created_at) "
  "SELECT 102, 'DOUBLE_XP', DATE_ADD(@vn_now, INTERVAL 25 MINUTE), @vn_now "
  "FROM DUAL WHERE EXISTS (SELECT 1 FROM information_schema.tables "
  "WHERE table_schema = DATABASE() AND table_name = 'user_active_effects');")
A("")

# ---------------------------------------------------------------- tài khoản sẵn có
A("-- 7. Tai khoan admin va 2 tai khoan ca nhan co san: chi don dep, khong doi mat khau")
A("UPDATE users SET display_name = 'Quản trị viên hệ thống' WHERE id = 1;")
A("UPDATE users SET coins = 500, current_energy = 25, exp = COALESCE(exp, 0) WHERE id IN (8, 9);")

with io.open(OUT, "w", encoding="utf-8", newline="\n") as f:
    f.write("\n".join(L))

print("hoc vien demo : %d" % len(LEARNERS))
print("bai NORMAL    : %d (chuoi mo khoa)" % len(normal_seq))
print("-> %s" % OUT)
