UPDATE users SET last_energy_reset_date = CAST(last_energy_reset_date AS DATETIME)
WHERE role = 'LEARNER' AND last_energy_reset_date IS NOT NULL;
ALTER TABLE users MODIFY COLUMN last_energy_reset_date DATETIME NULL AFTER max_energy;
