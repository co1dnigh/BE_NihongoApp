-- Fix gacha_items with invalid effect_type 'CONSUMABLE' → 'GACHA_ITEM'
UPDATE gacha_items SET effect_type = 'GACHA_ITEM', effect_value = 1 WHERE effect_type = 'CONSUMABLE';
