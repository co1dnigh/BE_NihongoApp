# NihongoApp Energy & Combo System

This file documents the energy per-question feature implemented in this branch.

## Energy Model

- **Max energy:** 25
- **Recovery rate:** 1 energy per hour (time-based, not daily)
- **Per-question cost:** 1 energy per answer whether correct or wrong
- **Wrong answer penalty:** +1 to +2 extra energy per wrong answer
- **Combo reward:** every 5 consecutive correct answers awards +1 to +5 random energy
- **Combo reset:** wrong answer resets the combo counter to 0
- **Recovery sources:**
  - Passive: 1 energy/hour, timer tracked via `last_energy_reset_date`
  - Practice: `POST /api/v1/users/me/energy/practice` (+1, no cooldown)
  - Watch ad: `POST /api/v1/users/me/energy/ads` (+1, 30 min cooldown)
  - Refill gems: `POST /api/v1/users/me/energy/refill` (full restore for 10 coins)

## Handling `data` on Submit

- The FE sends a `data` field in `SubmitLessonRequest`
- If the BE does not use `data`, that field is accepted and ignored
- No client-side fallback should depend on processed `data`

## Endpoints

- `GET /api/v1/users/me/energy`
- `POST /api/v1/users/me/energy/practice`
- `POST /api/v1/users/me/energy/ads`
- `POST /api/v1/users/me/energy/refill`
- `POST /api/v1/lessons/{id}/start`
- `POST /api/v1/lessons/{id}/submit`
- `POST /api/v1/lessons/{id}/cancel`
