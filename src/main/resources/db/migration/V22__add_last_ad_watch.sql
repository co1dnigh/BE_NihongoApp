-- V22: Add last_ad_watch_date for ad-based energy recovery cooldown
ALTER TABLE users ADD COLUMN last_ad_watch_date DATETIME NULL AFTER last_energy_reset_date;
