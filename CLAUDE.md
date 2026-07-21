# NIHONGO App — CLAUDE.md

## Project Overview
Japanese learning gamification app (JLPT N5/N4, Duolingo-style). Multi-platform (Web + Mobile) with gamification (streaks, hearts, gacha, leaderboard, leagues).

## Tech Stack (v3)
- Java 17+, Spring Boot 3.5
- MySQL 8.4 (Flyway migrations V1–V16, Hibernate for write-path only)
- Redis (AOF persistence) — banned users, leaderboard ZSET, idempotency keys, cache
- JWT HS256 (stateless access 15min, refresh 7d with DB state)
- Grafana Loki + Promtail (observability, <200MB RAM) — replaced ELK
- S3 + CDN (media), Firebase FCM (push notifications)
- App Store / Google Play Server API (IAP async webhook)

## Architecture Highlights (v3)
- JWT access token is fully stateless — Spring Security verifies signature + expiry only, NO Redis check per request
- Financial endpoint protection (`/wallet/**`, `/gacha/**`, `/iap/**`) checks only `banned_user_ids` Redis Set — small, rarely-written
- Learning hot-path uses Native SQL/JPQL DTO Projection — avoids JPA polymorphic JOIN overhead
- Idempotency: Layer 1 = Redis SET NX EX 600 (spam guard), Layer 2 = MySQL UNIQUE constraint (fallback defense)
- Streak = Lazy Evaluation (no cron batch job). Calculated on-demand at first request of the day using `last_lesson_completed_date` + timezone offset
- IAP fully async: receipt → staging table → webhook/poll verify → only then credit wallet
- Lock ordering: always lock `USER_WALLET` before `USER_GACHA_PITY` to prevent deadlock
- Correlation ID (MDC filter) — trace every request from controller to DB without full OpenTelemetry

## Run
```
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m"
```
App runs on `http://localhost:8080`.

## Test Users
| Username | Password | Role |
|----------|----------|------|
| testuser | test1234 | STUDENT |

## Key Endpoints
- `POST /api/v1/auth/login` — body: `{username, password}`
- `POST /api/v1/auth/refresh` — body: `{refreshToken}`
- `GET /actuator/health` — health check

## Sprint Status
- **Sprint 1** (Foundation + Security + Logging): ✅ DONE
  - JWT auth, CORS, MDC correlation ID, Flyway V1–V7, Redis, logging, error handling
  - Login/logout/refresh endpoints fully functional
  - CORS configured for localhost:3000,19006,8081,5173,4200
- **Sprint 2** (Learning Core): ✅ DONE — lesson completion, XP/gem rewards, streak calculation
- V15: fix wallet reason enum (SHOP_PURCHASE missing from DB enum)
- V16: seed learning content (courses, units, lessons, vocab, grammar, questions)
- Future sprints: Gamification expand, Wallet/Gacha polish, IAP, Admin/CMS, AI Vision, Mock Exam

## Critical Conventions
- Hot-path reads (lesson content, quiz questions) → Native SQL/DTO projection, never JPA polymorphic JOIN
- Financial writes → always use `@Transactional`, lock ordering, idempotency 2-layer
- Banned user check ONLY on `/wallet/**`, `/gacha/**`, `/iap/**` — keep stateless JWT fast elsewhere
- Migrations: V1–V16 applied. V15 fix wallet reason enum, V16 seed learning content
- Redis: only write/read through `RedisTemplate` beans in `RedisConfig.java`, no raw Jedis
