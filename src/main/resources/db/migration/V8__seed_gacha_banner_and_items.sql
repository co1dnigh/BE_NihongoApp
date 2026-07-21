INSERT INTO gacha_banners (id, name, is_active, cost_amount, start_at, end_at)
VALUES (1, 'Standard Banner', TRUE, 1000, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

INSERT INTO gacha_items (banner_id, item_name, rarity, drop_weight, effect_type, effect_value) VALUES
(1, 'Kyuubi no Yoko', 'SSR', 5, 'GACHA_ITEM', 1),
(1, 'Tiamat Fire', 'SSR', 5, 'GACHA_ITEM', 1),
(1, 'Hono no Tora', 'SR', 20, 'GACHA_ITEM', 1),
(1, 'Ice Spirit', 'SR', 20, 'GACHA_ITEM', 1),
(1, 'Stone Golem', 'R', 50, 'GACHA_ITEM', 1);
