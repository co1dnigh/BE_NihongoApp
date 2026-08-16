-- Add SHOP_PURCHASE and ITEM_CONSUME to coin_transactions transaction_type enum
ALTER TABLE coin_transactions
    MODIFY COLUMN transaction_type ENUM(
        'EARN_LESSON', 'BUY_ITEM', 'STREAK_BONUS', 'ADMIN_ADJUST',
        'DAILY_CHEST', 'BUY_STREAK_FREEZE',
        'SHOP_PURCHASE', 'ITEM_CONSUME'
    ) NOT NULL;