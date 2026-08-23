-- ============================================================
-- V33: Streak Calendar + Achievements
-- ============================================================
-- 1. user_streak_days: ghi nhận từng ngày user đã học (dùng vẽ heat map
--    trên streak calendar, tương tự Duolingo).
-- 2. achievements: định nghĩa thành tích (huy chương) — seed sẵn 14 achievements.
-- 3. user_achievements: ghi nhận achievement nào của user đã unlock, kèm progress.
-- ============================================================

-- --- 1. user_streak_days ------------------------------------------------
CREATE TABLE user_streak_days (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    study_date  DATE   NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_streak_day UNIQUE (user_id, study_date),
    CONSTRAINT fk_user_streak_day_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- --- 2. achievements -----------------------------------------------------
CREATE TABLE achievements (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    icon        VARCHAR(100) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    threshold   INT          NOT NULL DEFAULT 1,
    secret      BOOLEAN      NOT NULL DEFAULT FALSE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_achievement_code UNIQUE (code)
);

-- --- 3. user_achievements ------------------------------------------------
CREATE TABLE user_achievements (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    unlocked_at    TIMESTAMP NULL,
    progress       INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_id),
    CONSTRAINT fk_user_achievement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_achievement_achievement FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE
);

-- --- 4. users: cài đặt streak calendar (user có thể tắt heat map) -------
ALTER TABLE users
  ADD COLUMN streak_calendar_enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER streak_freeze_awarded;

-- --- Seed achievements ----------------------------------------------------
INSERT INTO achievements (code, name, description, icon, type, threshold, secret, active) VALUES
('FIRST_LESSON',      'Câu chuyện đầu tiên',     'Hoàn thành bài học đầu tiên',                 '🎯', 'LESSONS_COMPLETED', 1,  FALSE, TRUE),
('STREAK_3',           'Bắt đầu',                  'Đạt streak 3 ngày',                           '🔥', 'STREAK_MILESTONE', 3,  FALSE, TRUE),
('STREAK_7',           'Tuần học',                 'Đạt streak 7 ngày',                           '🔥', 'STREAK_MILESTONE', 7,  FALSE, TRUE),
('STREAK_30',          'Tháng bền vững',           'Đạt streak 30 ngày',                          '🔥', 'STREAK_MILESTONE', 30, FALSE, TRUE),
('STREAK_100',         'Tiềm năng vô hạn',         'Đạt streak 100 ngày',                         '🔥', 'STREAK_MILESTONE', 100,FALSE, TRUE),
('STREAK_365',         'Năm không ngừng',          'Đạt streak 365 ngày',                         '🔥', 'STREAK_MILESTONE', 365,TRUE,  TRUE),
('WEEK_WARRIOR',       'Chiến binh tuần',         'Học 7 ngày liên tiếp trong 1 tuần',           '⚔️', 'STREAK_MILESTONE', 7,  FALSE, TRUE),
('PERFECT_10',         'Hoàn hảo 10 lần',         'Hoàn thành 1 bài hoàn hảo (0 sai)',           '⭐', 'PERFECT_LESSON', 10, FALSE, TRUE),
('PERFECT_50',         'Hoàn hảo 50 lần',         'Hoàn thành 50 bài hoàn hảo',                  '⭐', 'PERFECT_LESSON', 50, FALSE, TRUE),
('TOPIC_MASTER',       'Bá chủ chủ đề',           'Hoàn thành 5 chủ đề khác nhau',               '📚', 'TOPIC_COMPLETED', 5,  FALSE, TRUE),
('TOPIC_EXPERT',       'Chuyên gia chủ đề',       'Hoàn thành 20 chủ đề khác nhau',              '📚', 'TOPIC_COMPLETED', 20, FALSE, TRUE),
('COIN_MASTER',        'Tỷ phú coin',             'Tích lũy được 1000 coin',                      '🪙', 'COIN_EARNED', 1000,FALSE, TRUE),
('STREAK_FREEZE_USER', 'Người bảo vệ streak',     'Sử dụng 1 lần Streak Freeze',                 '❄️', 'STREAK_FREEZE_USED', 1, FALSE, TRUE),
('EARLY_BIRD',         'Chim sớm',                 'Học trước 9h sáng',                           '🐦', 'DAILY_STUDY', 1,  TRUE,  TRUE),
('NIGHT_OWL',          'Cú đêm',                  'Học sau 22h',                                 '🦉', 'DAILY_STUDY', 1,  TRUE,  TRUE),
('COMEBACK',           'Quay trở lại',            'Reset streak sau khi đã đạt >= 7 ngày',       '🔄', 'STREAK_RESET', 7,  TRUE,  TRUE);