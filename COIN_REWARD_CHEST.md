# Hệ thống Coin, Daily Quest & Rương thưởng hàng ngày

> Tài liệu mô tả toàn bộ những gì đã được thêm vào backend trên nhánh `feature/coin-reward-chest`
> (dựa trên `integration/energy-streak-lesson-fix`, đã bao gồm sẵn fix lỗi mở khoá bài học
> xuyên Topic + hệ thống Energy/Streak của nhánh `EnegyStreak`).
>
> Tài liệu này được cập nhật dần theo các đợt commit trên nhánh: tính năng lõi coin/quest/chest,
> tài liệu Swagger/OpenAPI, bộ test, fix bug năng lượng, và gần nhất là **thiết kế lại toàn bộ
> kinh tế năng lượng** (xem [mục 4.5](#45-năng-lượng-energy)).

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
| `service/EnergyService.java` | Đổi `REFILL_COST_COINS` 10→400; và ở đợt sau, **thiết kế lại toàn bộ thang năng lượng**: `MAX_ENERGY` 5→25, hồi tự nhiên +1/giờ→+5/giờ (xem [4.5](#45-năng-lượng-energy)). |
| `service/impl/LessonAttemptServiceImpl.java` | Bổ sung trong `submitLesson()`: tính `coinsGained` theo loại bài + bonus "không sai câu nào", nhân tỉ lệ replay giống cách EXP đã làm; gọi `dailyQuestService.recordProgress(...)` cho từng loại quest liên quan; thưởng coin khi vừa **đạt đúng** mốc streak 7/30/100 ngày (tách biệt với `calculateStreakBonus` — thưởng EXP mỗi ngày streak ≥7 mà đồng đội đã thêm); **hoàn một phần năng lượng** đã trừ lúc `/start` tuỳ kết quả bài (xem [4.5](#45-năng-lượng-energy)). |
| `entity/User.java` *(đợt sau)* | Default `currentEnergy`/`maxEnergy` khi tạo Learner mới: 5→25. |

### DTO mới
`ChestOpenResponse` (`coinsRewarded`, `currentCoins`), `ChestStatusResponse` (`available`,
`alreadyOpenedToday`, `questsCompleted`, `questsRequired`), `QuestResponse` (`questId`, `title`,
`questType`, `currentProgress`, `targetValue`, `completed`). `SubmitLessonResponse` thêm field
`coinsEarned` (song song với `expEarned` đã có).

`UserStatsResponse` *(đợt sau)* — gộp toàn bộ trạng thái gamification (`level`, `exp`,
`currentLeague`, `coins`, `currentEnergy`, `maxEnergy`, `currentStreak`, `longestStreak`,
`streakFreezeCount`) trong 1 DTO, trả về bởi `GET /api/v1/users/me`. Xem [4.6](#46-api-tổng-hợp-trạng-thái-get-usersme).

### Exception mới
`ChestNotAvailableException` (400) — theo đúng pattern `LessonLockedException`.
`GlobalExceptionHandler` được vá thêm 2 handler còn thiếu: `InsufficientCoinsException` (bug có
sẵn từ nhánh `EnegyStreak` — exception này được `EnergyController.refill()` ném ra nhưng chưa từng
được bắt, sẽ rơi vào handler 500 chung) và `ChestNotAvailableException`.

### Controller mới/sửa
`ChestController`, `QuestController` (mới); `StreakController` được mở rộng thêm endpoint mua
Freeze; `UserController` *(đợt sau)* thêm `GET /me` trả `UserStatsResponse` (xem
[API Reference](#5-api-reference)).

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

### 4.5 Năng lượng (Energy)

**Đã thiết kế lại** so với đợt merge `EnegyStreak` ban đầu. Ban đầu (nhánh `EnegyStreak`) đặt `max_energy = 5`, và khi merge vào đây thì
`resolveEntryCost()` mặc định fallback về `questionsPerSession` (10) — **10 > 5** khiến không
learner nào start được bài nào cả (xem mục 8 lịch sử). Sau khi cân nhắc, đã **thiết kế lại toàn
bộ thang đo** thay vì chỉ vá số:

| Thông số | Giá trị |
|---|---|
| `max_energy` mặc định user mới (`User.onCreate`) | **25** |
| Chi phí bắt đầu 1 bài (`DEFAULT_ENTRY_COST_ENERGY`) | **10** |
| Hoàn lại nếu bài **hoàn hảo** (`totalMistakes == 0`) | **+5** (tốn ròng 5) |
| Hoàn lại nếu bài **khá** (`totalMistakes` 1–2) | **+2** (tốn ròng 8) |
| Hồi tự nhiên (`RECOVERY_AMOUNT_PER_INTERVAL`/giờ) | **+5/giờ** (đầy sau 5 tiếng) |
| Xem quảng cáo (`watchAd`) | **+5**, cooldown 30 phút (không đổi) |
| Hồi đầy bằng coin (`refillWithCoins`) | **400 coin** (không đổi — vốn đã tính cho thang 25) |

**Cơ chế hoàn năng lượng** nằm trong `LessonAttemptServiceImpl.submitLesson()`, bên trong khối
`if (passed)`, dùng lại đúng `req.getTotalMistakes()` đã có sẵn (không cần field mới):

```java
int entryCostCharged = isReplay ? 0 : resolveEntryCost(lesson);
int energyRefund = perfectLesson ? PERFECT_LESSON_ENERGY_REFUND
        : (goodLesson ? GOOD_LESSON_ENERGY_REFUND : 0);
energyRefund = Math.min(energyRefund, entryCostCharged); // chan exploit, xem duoi
```

**Chặn exploit farm năng lượng**: nếu không cap `energyRefund` ở `entryCostCharged`, user có thể
replay vô hạn 1 bài đã hoàn thành thật hoàn hảo để nhận +5 năng lượng miễn phí mỗi lần (vì replay
vốn không tốn gì lúc `/start`). Cap ở đúng số **thực sự đã trừ** giải quyết đồng thời 2 case:
replay (`entryCostCharged = 0` → không hoàn gì) và lesson có `configJson.entryCostEnergy` rẻ hơn
mức hoàn mặc định (không hoàn vượt quá số đã trừ).

**Migration**: `V23__increase_max_energy_to_25.sql` nâng `max_energy` của user LEARNER đã tồn tại
lên 25 (chỉ nâng trần, không cộng thêm `current_energy` — user tự hồi dần qua cơ chế +5/giờ).
Lưu ý `V22` đã được dùng trước đó cho đợt đổi cơ chế hồi theo giờ + thêm `watchAd`, nên bản vá này
là `V23`.

### 4.6 API tổng hợp trạng thái (`GET /users/me`)

Giải quyết trực tiếp phản hồi từ FE: trước đây **không có endpoint nào** trả về đủ
exp/level/coins/energy/streak trong 1 lần gọi — `AuthResponse.user` (lúc login) chỉ có
`id/email/displayName/username/role`, còn `coins`/`exp` thậm chí chưa từng được trả về ở bất kỳ
đâu. Hệ quả: FE phải chờ user submit bài / mở rương mới có dữ liệu thật, còn lại hiển thị giá trị
mặc định hardcode phía client.

- `dto/response/UserStatsResponse.java` *(mới)*: gộp `id/email/displayName/username/role` +
  `level/exp/currentLeague` + `coins` + `currentEnergy/maxEnergy` + `currentStreak/longestStreak/
  streakFreezeCount`.
- `UserService.getMyStats(String email)` *(mới)*, implement trong `UserServiceImpl`: fetch user
  theo email, gọi `energyService.recoverEnergy(userId)` **trước khi đọc** để `currentEnergy` luôn
  chính xác (không "đứng hình" tới khi FE gọi riêng `/energy`), rồi map sang DTO.
- `UserController.getMyStats()` *(mới)*: `GET /api/v1/users/me`.

Cố tình **không** gộp thêm `lastEnergyResetDate`/`lastStreakDate` vào response này — 2 field đó
chỉ cần thiết cho việc tính cooldown, đã có sẵn ở `GET /energy`/`GET /streak` riêng, giữ endpoint
này gọn cho đúng mục đích "hydrate số liệu hiển thị".

---

## 5. API Reference

Tất cả endpoint dưới đây yêu cầu JWT (`Authorization: Bearer <token>`), trừ khi ghi chú khác.

| Method | Path | Mô tả |
|---|---|---|
| GET | `/api/v1/users/me` | **Trạng thái tổng hợp** (level/exp/league/coins/energy/streak) — gọi ngay sau login |
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
context load). Giờ có **65 test**, chia 3 nhóm:

### Unit test (Mockito, không cần DB)
| File | Test |
|---|---|
| `DailyQuestServiceTest` | Gán quest ngẫu nhiên, cộng tiến độ + cap + đánh completed, bỏ qua quest sai loại/đã xong |
| `ChestServiceTest` | Trạng thái rương, chặn mở khi thiếu điều kiện, mở thành công đúng khoảng thưởng |
| `StreakServiceTest` | *(chỉ scope `buyStreakFreeze`)* thiếu/đủ coin |
| `EnergyServiceTest` | Hồi tự nhiên +5/giờ (cap ở max), xem QC (+5, chặn khi đầy/chưa hết cooldown), trừ năng lượng, hồi đầy bằng coin |
| `UserServiceImplTest` | *(chỉ scope `getMyStats`)* trả đủ field, gọi `recoverEnergy` trước khi đọc, 404 khi không tìm thấy user |
| `LessonUnlockPolicyTest` | Toàn bộ thuật toán mở khoá, đặc biệt **regression test bug cross-topic** đã fix trước đó |
| `RoadmapServiceImplTest` | Dùng `LessonUnlockPolicy` thật (không mock) để test nguyên luồng `GET /topics` |
| `LessonAttemptServiceImplTest` | Coin/quest/streak-milestone + 3 tier hoàn năng lượng (hoàn hảo/khá/tệ) trong `submitLesson`, riêng 1 test cho **exploit-guard** (replay không được hoàn năng lượng), cộng cả `startLesson`/`cancelLesson` (unlock, trừ/hoàn năng lượng, giới hạn số câu hỏi) |

### Integration test (MockMvc + MySQL docker thật)
`CoinQuestChestIntegrationTest`: chạy nguyên luồng HTTP thật (đăng ký user → nộp bài hoàn thành
quest → mở rương → mở lần 2 bị chặn → mua Streak Freeze thiếu/đủ coin). Class gắn
`@Transactional` để tự rollback sau mỗi test, không làm bẩn DB dev.

**Chạy test**: `./mvnw test` (cần Docker MySQL đang chạy cho integration test + `NihongoAppApplicationTests`).

---

## 8. Vấn đề đã biết & backlog

### ✅ Đã fix: bug "không start được bài học nào" (entry cost 10 > max energy 5)
Ban đầu `resolveEntryCost()` mặc định 10 nhưng `max_energy` (nhánh `EnegyStreak`) chỉ có 5, khiến
không learner nào start được bài nào. Đã xử lý bằng cách **thiết kế lại toàn bộ thang đo năng
lượng** (không chỉ vá số) — xem chi tiết đầy đủ ở [mục 4.5](#45-năng-lượng-energy). Đã verify lại
bằng API thật: user mới 25/25 → start trừ còn 15 → submit hoàn hảo hoàn lại 20.

### ✅ Đã fix: thiếu API tổng hợp trạng thái (FE bị reset về mặc định khi mở lại app)

FE báo: exp/streak/năng lượng chỉ trả về sau khi làm 1 hành động nào đó, nên mở lại app/login lại
là không có gì để hiển thị ngoài giá trị mặc định hardcode phía client. Đã fix bằng
`GET /api/v1/users/me` — xem [mục 4.6](#46-api-tổng-hợp-trạng-thái-get-usersme). Đã verify: user
vừa đăng ký xong, gọi ngay API này (chưa làm gì cả) vẫn trả về đủ số liệu thật từ DB.

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
SERVER_PORT=8090 ./mvnw spring-boot:run  # Flyway tu ap V20-V23

# Đăng ký + đăng nhập lấy token
curl -X POST localhost:8090/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Test1234!","displayName":"Demo"}'
TOKEN=$(curl -s -X POST localhost:8090/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Test1234!"}' | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

curl localhost:8090/api/v1/users/me -H "Authorization: Bearer $TOKEN"  # toan bo stats, chua lam gi cung co
curl localhost:8090/api/v1/users/me/energy -H "Authorization: Bearer $TOKEN"   # 25/25
curl -X POST localhost:8090/api/v1/lessons/1/start -H "Authorization: Bearer $TOKEN"  # tru con 15
curl localhost:8090/api/v1/users/me/quests -H "Authorization: Bearer $TOKEN"
curl -X POST localhost:8090/api/v1/lessons/1/submit -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"totalQuestions":10,"totalCorrect":10,"totalMistakes":0}'
  # hoan hao -> hoan +5, currentEnergy tra ve = 20
curl localhost:8090/api/v1/users/me/chest -H "Authorization: Bearer $TOKEN"
```
