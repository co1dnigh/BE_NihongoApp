ALTER TABLE users
    ADD COLUMN last_chest_opened_date DATE NULL AFTER last_energy_reset_date;

ALTER TABLE coin_transactions
    MODIFY COLUMN transaction_type ENUM(
        'EARN_LESSON', 'BUY_ITEM', 'STREAK_BONUS', 'ADMIN_ADJUST',
        'DAILY_CHEST', 'BUY_STREAK_FREEZE'
    ) NOT NULL;
