-- Fix user_inventories foreign key to allow both gacha_items and shop_items
-- Drop the foreign key constraint to gacha_items
ALTER TABLE user_inventories
    DROP FOREIGN KEY user_inventories_ibfk_2;