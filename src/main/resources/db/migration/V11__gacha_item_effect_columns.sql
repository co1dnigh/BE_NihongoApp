-- Add effect_type and effect_value to gacha_items (GachaService uses these fields)
ALTER TABLE gacha_items
  ADD COLUMN effect_type VARCHAR(20) NOT NULL DEFAULT 'CONSUMABLE' AFTER drop_weight,
  ADD COLUMN effect_value INT NOT NULL DEFAULT 1 AFTER effect_type;

UPDATE gacha_items SET effect_type = 'CONSUMABLE', effect_value = 1 WHERE effect_type IS NULL;
