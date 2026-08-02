-- V19: Reset max_energy ve 5 (giong Duolingo), cap current_energy theo max_energy
-- Cap nhat existing users
UPDATE users
SET max_energy = 5,
    current_energy = LEAST(current_energy, 5),
    last_energy_reset_date = CURRENT_DATE
WHERE role = 'LEARNER'
  AND (max_energy > 5 OR last_energy_reset_date IS NULL);
