# Hệ thống Coin, Daily Quest & Rương thưởng hàng ngày

> Tài liệu mô tả toàn bộ những gì đã được thêm vào backend trên nhánh `feature/coin-reward-chest`
> (dựa trên `integration/energy-streak-lesson-fix`, đã bao gồm sẵn fix lỗi mở khoá bài học
> xuyên Topic + hệ thống Energy/Streak của nhánh `EnegyStreak`).
>
> Gồm 3 commit: tính năng lõi (`10735c8`), tài liệu Swagger/OpenAPI (`e2fb53d`), và bộ test
> (`8706c60`).

## Mục lục

1. [Bối cảnh & mục tiêu](#1-bối-cảnh--mục-tiêu)
2. [Database schema mới](#2-database-schema-mới)
3. [Kiến trúc code](#3-kiến-trúc-code)
4. [Chi tiết nghiệp vụ](#4-chi-tiết-nghiệp-vụ)
5. [API Reference](#5-api-reference)
6. [Swagger / OpenAPI](#6-swagger--openapi)
7. [Testing](#7-testing)
8. [Vấn đề đã biết & backlog](#8-vấn-đề-đã-biết--backlog)
9. [Chạy thử nhanh](#9-chạy-thử-nhanh)

---

## 1. Bối cảnh & mục tiêu

Trước khi có thay đổi này, hệ thống chỉ có `User.coins` là một field mặc định `0`, **không có bất
kỳ nguồn kiếm (faucet) hay nguồn tiêu (sink) nào** — coin tồn tại trên schema nhưng chưa từng được
đọc/ghi ở đâu cả (ngoại trừ nhánh `EnegyStreak` của đồng đội mới thêm đúng 1 sink:
`EnergyService.refillWithCoins()`).

Mục tiêu của thay đổi này: xây dựng một vòng lặp kinh tế coin hoàn chỉnh theo mô hình Duolingo
(faucet → sink, có "rương thưởng" tạo hiệu ứng variable-reward), giới hạn ở một MVP có thể triển
khai và kiểm thử được ngay, cụ thể:

- **Làm ngay**: faucet cơ bản (hoàn thành bài học, mốc streak), Daily Quest engine (3 quest/ngày,
  gate việc mở rương), Rương thưởng (random coin), sink Streak Freeze, cập nhật giá refill năng
  lượng theo thiết kế kinh tế mới.
- **Backlog, chưa làm**: Friend Quests, Monthly Quests, thưởng theo hạng đấu (League), nhân đôi
  coin qua quảng cáo, nhiều cấp độ rương, pity system, vé Timed Challenge bằng coin, admin CRUD
  cho `quest_definitions` (hiện seed cứng qua migration).

---

## 2. Database schema mới

### Migration `V20__create_quest_tables.sql`

```sql
CREATE TABLE quest_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    quest_type ENUM('COMPLETE_LESSONS', 'CORRECT_ANSWERS', 'PERFECT_LESSON') NOT NULL,
    target_value INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_daily_quests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quest_definition_id BIGINT NOT NULL,
    quest_date DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    quest_type ENUM('COMPLETE_LESSONS', 'CORRECT_ANSWERS', 'PERFECT_LESSON') NOT NULL,
    target_value INT NOT NULL,
    current_progress INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quest_definition_id) REFERENCES quest_definitions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_quest_date (user_id, quest_definition_id, quest_date)
);
```

Seed sẵn 5 quest definition để có dữ liệu random ngay khi chạy:

| Title | Type | Target |
|---|---|---|
| Hoàn thành 1 bài học | `COMPLETE_LESSONS` | 1 |
| Hoàn thành 2 bài học | `COMPLETE_LESSONS` | 2 |
| Trả lời đúng 10 câu | `CORRECT_ANSWERS` | 10 |
| Trả lời đúng 20 câu | `CORRECT_ANSWERS` | 20 |
| Hoàn thành 1 bài không sai câu nào | `PERFECT_LESSON` | 1 |

`quest_type`/`target_value`/`title` được **copy sang `user_daily_quests` tại thời điểm gán**
(không tham chiếu động tới `quest_definitions`), để sau này admin sửa definition không ảnh hưởng
ngược lại quest đã gán cho user trong quá khứ.

### Migration `V21__add_chest_tracking_and_coin_types.sql`

```sql
ALTER TABLE users
    ADD COLUMN last_chest_opened_date DATE NULL AFTER last_energy_reset_date;

ALTER TABLE coin_transactions
    MODIFY COLUMN transaction_type ENUM(
        'EARN_LESSON', 'BUY_ITEM', 'STREAK_BONUS', 'ADMIN_ADJUST',
        'DAILY_CHEST', 'BUY_STREAK_FREEZE'
    ) NOT NULL;
```

`coin_transactions` vốn đã tồn tại từ rất lâu (migration `V4`) nhưng **chưa từng có entity Java
ánh xạ tới** — lần này mới thêm `CoinTransaction.java` để thực sự ghi log giao dịch.

---

## 3. Kiến trúc code

### Entity mới
| File | Mô tả |
|---|---|
| `entity/CoinTransaction.java` | Log mọi thay đổi số dư coin. `amount` âm/dương, `transactionType` enum 6 giá trị, `referenceId` tuỳ ngữ cảnh (vd `lesson_id`). |
| `entity/QuestDefinition.java` | Danh mục quest có thể được gán ngẫu nhiên. |
| `entity/UserDailyQuest.java` | 1 quest cụ thể đã gán cho 1 user trong 1 ngày, có `currentProgress`/`completed`. |

### Entity sửa
| File | Thay đổi |
|---|---|
| `entity/User.java` | Thêm field `lastChestOpenedDate` (LocalDate) — mốc "đã mở rương hôm nay chưa", theo đúng pattern `lastEnergyResetDate`/`lastStreakDate` đã có. |

### Repository mới
`CoinTransactionRepository`, `QuestDefinitionRepository` (`findAllByActiveTrue()`),
`UserDailyQuestRepository` (`findAllByUserIdAndQuestDate(userId, date)`).

### Service mới
| File | Trách nhiệm |
|---|---|
| `service/DailyQuestService.java` | `ensureTodayQuests()` gán "lười" (lazy) 3 quest ngẫu nhiên nếu chưa có cho hôm nay (không cần job chạy nền lúc nửa đêm); `recordProgress(userId, type, amount)` cộng dồn + cap ở target + đánh dấu hoàn thành; `areAllTodayQuestsCompleted()`. |
| `service/ChestService.java` | `getStatus()` trả trạng thái rương; `openChest()` kiểm tra điều kiện (đủ quest + chưa mở hôm nay), roll ngẫu nhiên **30–100 coin** bằng `ThreadLocalRandom` ở server, cộng coin, set `lastChestOpenedDate`, ghi `CoinTransaction(DAILY_CHEST)`. |

### Service sửa
| File | Thay đổi |
|---|---|
| `service/StreakService.java` | Thêm `buyStreakFreeze(userId)`: giá cố định **200 coin**, tăng `streakFreezeCount`, ném `InsufficientCoinsException` nếu thiếu coin. Không đụng tới `checkAndUpdateStreak()` có sẵn của nhánh `EnegyStreak`. |
| `service/EnergyService.java` | Đổi hằng số `REFILL_COST_COINS` từ **10 → 400** coin/lần hồi đầy năng lượng, theo đúng thiết kế kinh tế (hồi tim tốn 350-500 gems). |
| `service/impl/LessonAttemptServiceImpl.java` | Bổ sung trong `submitLesson()`: tính `coinsGained` theo loại bài + bonus "không sai câu nào", nhân tỉ lệ replay giống cách EXP đã làm; gọi `dailyQuestService.recordProgress(...)` cho từng loại quest liên quan; thưởng coin khi vừa **đạt đúng** mốc streak 7/30/100 ngày (tách biệt với `calculateStreakBonus` — thưởng EXP mỗi ngày streak ≥7 mà đồng đội đã thêm). |

### DTO mới
`ChestOpenResponse` (`coinsRewarded`, `currentCoins`), `ChestStatusResponse` (`available`,
`alreadyOpenedToday`, `questsCompleted`, `questsRequired`), `QuestResponse` (`questId`, `title`,
`questType`, `currentProgress`, `targetValue`, `completed`). `SubmitLessonResponse` thêm field
`coinsEarned` (song song với `expEarned` đã có).

### Exception mới
`ChestNotAvailableException` (400) — theo đúng pattern `LessonLockedException`.
`GlobalExceptionHandler` được vá thêm 2 handler còn thiếu: `InsufficientCoinsException` (bug có
sẵn từ nhánh `EnegyStreak` — exception này được `EnergyController.refill()` ném ra nhưng chưa từng
được bắt, sẽ rơi vào handler 500 chung) và `ChestNotAvailableException`.

### Controller mới/sửa
`ChestController`, `QuestController` (mới); `StreakController` được mở rộng thêm endpoint mua
Freeze (xem [API Reference](#5-api-reference)).

---

## 4. Chi tiết nghiệp vụ

### 4.1 Faucet — kiếm coin

| Nguồn | Điều kiện | Coin |
|---|---|---|
| Hoàn thành bài NORMAL | pass | 8 (+4 nếu `totalMistakes == 0`) |
| Hoàn thành bài TIMED_REVIEW | pass | 6 (+3 nếu perfect) |
| Hoàn thành JUMP_TEST | `heartsRemaining > 0` | 20 (flat, không có bonus perfect) |
| Mốc streak | `currentStreak` **đúng bằng** 7 / 30 / 100 | 50 / 200 / 1000 |
| Rương thưởng hàng ngày | đủ điều kiện mở | ngẫu nhiên 30–100 |

**Replay** (làm lại bài đã COMPLETED): coin bị nhân theo `resolveReplayExpRatio(lesson)` — mặc
định 30%, admin override qua `configJson.replayExpRatio` — dùng chung công thức với EXP để tránh
exploit farm coin bằng cách replay liên tục.

Mọi lần cộng coin đều ghi 1 dòng `coin_transactions` tương ứng (`EARN_LESSON`, `STREAK_BONUS`,
`DAILY_CHEST`).

### 4.2 Sink — tiêu coin

| Sink | Giá | Ghi chú |
|---|---|---|
| Hồi đầy năng lượng (`EnergyService.refillWithCoins`) | 400 coin | Đã có sẵn từ `EnegyStreak`, session này chỉ đổi giá 10→400. |
| Mua 1 lượt Streak Freeze | 200 coin | Mới thêm. |

### 4.3 Daily Quest

- Mỗi ngày, lần gọi `GET /users/me/quests` (hoặc lần `submitLesson` đầu tiên trong ngày) đầu tiên
  sẽ **tự động** random 3 trong 5 `QuestDefinition` đang `active` và tạo `UserDailyQuest` — không
  cần cron job.
- Tiến độ được cập nhật ngay trong `submitLesson()` mỗi khi `passed == true`:
  - `COMPLETE_LESSONS` +1
  - `CORRECT_ANSWERS` + `totalCorrect` của lần nộp đó
  - `PERFECT_LESSON` +1 nếu `totalMistakes == 0`
- Quest có thể đã hoàn thành hoặc sai loại thì bị bỏ qua khi cộng tiến độ (không tràn, không ghi
  đè).

### 4.4 Rương thưởng (Daily Chest)

Điều kiện mở (`ChestService.openChest`):
1. **Chưa mở hôm nay** (`user.lastChestOpenedDate != today`).
2. **Đã hoàn thành cả 3 Daily Quest** hôm nay.

Không thoả 1 trong 2 → `ChestNotAvailableException` (400). Thoả cả 2 → roll ngẫu nhiên
**30–100 coin** ở server (`ThreadLocalRandom`, không cho client tự roll để tránh gian lận), cộng
vào `user.coins`, set `lastChestOpenedDate = today`, ghi `CoinTransaction(DAILY_CHEST)`.

---

## 5. API Reference

Tất cả endpoint dưới đây yêu cầu JWT (`Authorization: Bearer <token>`), trừ khi ghi chú khác.

| Method | Path | Mô tả |
|---|---|---|
| GET | `/api/v1/users/me/quests` | Xem 3 Daily Quest hôm nay (tự tạo nếu chưa có) |
| GET | `/api/v1/users/me/chest` | Xem trạng thái rương thưởng hôm nay |
| POST | `/api/v1/users/me/chest/open` | Mở rương, nhận coin ngẫu nhiên 30–100 |
| POST | `/api/v1/users/me/streak/freeze/buy` | Mua 1 lượt Streak Freeze (200 coin) |
| POST | `/api/v1/users/me/energy/refill` | *(đã có, chỉ đổi giá)* Hồi đầy năng lượng (400 coin) |

`POST /api/v1/lessons/{id}/submit` (đã có từ trước) giờ trả thêm field `coinsEarned` trong
response, và là nơi kích hoạt toàn bộ luồng cộng coin/cập nhật quest/thưởng streak nói trên.

---

## 6. Swagger / OpenAPI

- Thêm dependency `springdoc-openapi-starter-webmvc-ui:3.1.0` (bản 3.x — tương thích Spring Boot
  4.1 / Spring Framework 7, **khác** với bản 2.x quen thuộc cho Spring Boot 3).
- `config/OpenApiConfig.java`: khai báo info API + security scheme `bearerAuth` (HTTP Bearer JWT),
  áp dụng mặc định cho mọi endpoint.
- `SecurityConfig.java`: mở public `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` (trước
  đó bị `anyRequest().authenticated()` chặn).
- Toàn bộ 10 controller (44 API, riêng `AdminController` mount 2 tiền tố `/api/v1/admin` +
  `/api/admin` nên Swagger hiển thị 64 dòng) đều có `@Tag` + `@Operation(summary = ...)`.
- `AuthController` gắn `@SecurityRequirements` rỗng (không cần token cho register/login).

**Truy cập**: `http://localhost:8090/swagger-ui/index.html`, spec JSON tại `/v3/api-docs`. Login
qua `/api/v1/auth/login` lấy `accessToken`, bấm **Authorize** trên Swagger UI, dán token (không
cần tiền tố `Bearer `).

---

## 7. Testing

Trước thay đổi này, project chỉ có 1 test placeholder (`NihongoAppApplicationTests`, chỉ check
context load). Giờ có **49 test**, chia 3 nhóm:

### Unit test (Mockito, không cần DB)
| File | Test |
|---|---|
| `DailyQuestServiceTest` | Gán quest ngẫu nhiên, cộng tiến độ + cap + đánh completed, bỏ qua quest sai loại/đã xong |
| `ChestServiceTest` | Trạng thái rương, chặn mở khi thiếu điều kiện, mở thành công đúng khoảng thưởng |
| `StreakServiceTest` | *(chỉ scope `buyStreakFreeze`)* thiếu/đủ coin |
| `LessonUnlockPolicyTest` | Toàn bộ thuật toán mở khoá, đặc biệt **regression test bug cross-topic** đã fix trước đó |
| `RoadmapServiceImplTest` | Dùng `LessonUnlockPolicy` thật (không mock) để test nguyên luồng `GET /topics` |
| `LessonAttemptServiceImplTest` | Coin/quest/streak-milestone trong `submitLesson`, cộng cả `startLesson`/`cancelLesson` (unlock, trừ/hoàn năng lượng, giới hạn số câu hỏi) |

### Integration test (MockMvc + MySQL docker thật)
`CoinQuestChestIntegrationTest`: chạy nguyên luồng HTTP thật (đăng ký user → nộp bài hoàn thành
quest → mở rương → mở lần 2 bị chặn → mua Streak Freeze thiếu/đủ coin). Class gắn
`@Transactional` để tự rollback sau mỗi test, không làm bẩn DB dev.

**Chạy test**: `./mvnw test` (cần Docker MySQL đang chạy cho integration test + `NihongoAppApplicationTests`).

---

## 8. Vấn đề đã biết & backlog

### ⚠️ Bug đang treo — chặn toàn bộ luồng học bài
`LessonAttemptServiceImpl.resolveEntryCost()` mặc định tốn **10 năng lượng** để bắt đầu 1 bài học
(khi lesson không có `configJson.entryCostEnergy`), nhưng nhánh `EnegyStreak` đã hạ `max_energy`
mặc định xuống **5**. Kết quả: **không learner nào start được bài học nào cả**, kể cả khi đầy năng
lượng. Đã verify bằng API thật (`POST /lessons/{id}/start` → `400 Khong du nang luong. Can 10,
hien co 5`). Cần đồng đội quyết định: hạ cost mặc định xuống ≤5, hoặc set
`configJson.entryCostEnergy` riêng cho từng lesson.

### Backlog (đã bàn nhưng cố tình để sau, không thuộc scope lần này)
- Friend Quests, Monthly Quests
- Thưởng theo hạng đấu (League rank reward)
- Nhân đôi coin qua xem quảng cáo
- Nhiều cấp độ/độ hiếm của rương (hiện chỉ có 1 loại)
- Pity system (tăng tỉ lệ thưởng cao nếu roll thấp nhiều lần liên tiếp)
- Vé vào Timed Challenge bằng coin
- Admin CRUD cho `quest_definitions` (hiện chỉ seed cứng qua migration V20)
- Bảng `gacha_items` / `user_inventories` / `inventory_transactions` (item-drop gacha đời cũ,
  không dùng cho hệ rương-thưởng-coin thuần tuý lần này)

---

## 9. Chạy thử nhanh

```bash
docker compose up -d                     # MySQL local
SERVER_PORT=8090 ./mvnw spring-boot:run  # Flyway tự áp V20/V21

# Đăng ký + đăng nhập lấy token
curl -X POST localhost:8090/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Test1234!","displayName":"Demo"}'
TOKEN=$(curl -s -X POST localhost:8090/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Test1234!"}' | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

curl localhost:8090/api/v1/users/me/quests -H "Authorization: Bearer $TOKEN"
curl -X POST localhost:8090/api/v1/lessons/1/submit -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"totalQuestions":10,"totalCorrect":10,"totalMistakes":0}'
curl localhost:8090/api/v1/users/me/chest -H "Authorization: Bearer $TOKEN"
```

> Lưu ý: do bug ở mục 8, `POST /lessons/{id}/start` hiện sẽ báo thiếu năng lượng với user mới —
> có thể test `/submit` trực tiếp (không cần gọi `/start` trước) để bỏ qua giới hạn này tạm thời.
