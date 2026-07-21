INSERT IGNORE INTO user_wallet (user_id, gold_balance, gem_balance, version)
SELECT id, 10000, 500, 0 FROM users WHERE username = 'testuser';
