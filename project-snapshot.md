# Project Snapshot — BE_NihongoApp

> Snapshot dựa trên phân tích code thực tế tại thời điểm viết (nhánh `feature/social-feed`, tạo từ
> `feature/mistake-bank` sau khi nhánh đó đã merge `feature/achivements` (hệ Thành tích + Streak
> calendar + bảng chữ cái) và `fix/shop_quantity` (UserActiveEffect thay cho
> `UserInventory.expiresAt`, thêm `quantity`), rồi triển khai thêm module Bản tin cộng đồng
> (Social Feed) — 54 commit, 189 file Java main + 18 file test, 37 migration Flyway. Không có
> API key/token/mật khẩu thật nào bị lộ trong tài liệu này — mọi chỗ có giá trị nhạy cảm đều bị
> che theo yêu cầu.

---

## 1. TỔNG QUAN

**⚠️ Lưu ý quan trọng trước khi đọc**: prompt gốc yêu cầu xác định "nền tảng (iOS/Android/
cross-platform)" và đọc `package.json`/`pubspec.yaml`/`build.gradle` — đó là các dấu hiệu của
một app **frontend/mobile**. Repo này thực chất là **backend REST API thuần** (Java/Spring Boot,
build bằng Maven qua `pom.xml`), không có bất kỳ code UI/mobile nào. Không có `build.gradle`,
`pubspec.yaml`; có 1 file `package-lock.json` ở root nhưng nội dung rỗng (`"packages": {}`, không
có `package.json` đi kèm) — gần như chắc chắn là file thừa/tạo nhầm, không phản ánh dependency gì
thật. Phần dưới đây phân tích dựa trên bản chất thật của repo (backend API).

**App làm gì**: Backend cho một ứng dụng học tiếng Nhật kiểu Duolingo — package Java tên
`com.example.nihongo_app` ("nihongo" = tiếng Nhật). Cung cấp REST API cho:
- Lộ trình học theo Topic → Lesson (3 loại bài: `NORMAL`, `TIMED_REVIEW`, `JUMP_TEST`), có cơ chế
  mở khoá tuần tự.
- Hệ gamification đầy đủ: EXP/Level/**Rank** (hạng đấu kèm Leaderboard — đã thay thế hệ `League`
  cũ), Streak (chuỗi ngày học), Energy (năng lượng giới hạn số bài học/ngày), Coin, Daily Quest,
  Rương thưởng (Chest), **Shop** (mua vật phẩm/powerup bằng coin, có túi đồ riêng — vừa fix bug
  `quantity`, tách timed-effect ra `UserActiveEffect` riêng), **Mistake Bank** (ngân hàng lỗi sai +
  phiên ôn tập riêng), **Achievements** (thành tích unlock theo ngưỡng/sự kiện + streak calendar),
  **Bảng chữ cái** (Hiragana/Katakana, luyện tập có chấm điểm) — xem mục 5a.
- **Bản tin cộng đồng (Social Feed, mới thêm)**: follow 1 chiều, feed tổng hợp (bài của mình +
  người đang follow, cursor pagination), đăng trạng thái, like, comment; tự động đăng bài khi
  unlock thành tích (`SYSTEM_ACHIEVEMENT`) — xem mục 5a.
- Quản trị nội dung (Admin CRUD Topic/Lesson/Question/Alphabet), xác thực JWT, upload file, tìm
  bạn/follow.
- Có sẵn schema DB (nhưng **chưa có code**) cho thi thử JLPT; ngoài ra có 1 entity
  (`ThematicSection`) cũng chưa nối vào controller/service nào (xem mục 5c).

**Nền tảng**: Backend độc lập (headless), phục vụ bất kỳ client nào gọi REST API (web/mobile/
app riêng) — không có ràng buộc iOS/Android cụ thể trong repo này.

**Framework & version chính** (đọc từ `pom.xml`):
```xml
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-parent</artifactId>
<version>4.1.0</version>
...
<java.version>17</java.version>
```
- Spring Boot **4.1.0** (kéo theo Spring Framework 7.0.8, Spring Security 7.1.0, Hibernate ORM
  7.4.1, Tomcat embedded 11.0.22) — bản rất mới, tách nhiều module test riêng (vd
  `spring-boot-starter-webmvc-test` thay vì `spring-boot-starter-test` gộp chung như bản 3.x).
- Java **17**, build bằng **Maven** (`mvnw`/`mvnw.cmd`).

---

## 2. CẤU TRÚC THƯ MỤC

Cây thư mục đúng 3 cấp theo yêu cầu (bỏ `target/`, `.git/`, `uploads/`) — với dự án Maven, cấu
trúc thật (package Java) nằm sâu hơn 3 cấp nên cây này khá "trống":

```
.
├── .github/workflows/ci.yml          # CI thật (build + test tren MySQL that qua GitHub Actions)
├── .github/modernize/java-upgrade/   # tooling hook cua 1 extension VS Code, khong phai code du an
├── .mvn/wrapper/                     # Maven wrapper
├── .vscode/                          # cấu hình editor
├── src/
│   ├── main/
│   │   ├── java/                     # -> com/example/nihongo_app/... (xem bên dưới)
│   │   └── resources/                # application.yml + db/migration/
│   └── test/
│       └── java/                     # -> com/example/nihongo_app/... (mirror cấu trúc main)
├── docker-compose.yml                # MySQL 8.4 + phpMyAdmin
├── pom.xml
├── mvnw / mvnw.cmd
├── package-lock.json                 # file thừa, nội dung rỗng (xem mục 1)
├── .claude/settings.json             # settings Claude Code cá nhân của 1 đồng đội, lọt vào commit
├── README.md, DATABASE_SETUP.md      # tài liệu setup (biến môi trường, kết nối DB qua Docker)
├── FE_API_GUIDE_*.md (3 file)        # tài liệu API cho FE: coin/quest/chest/energy, shop/rank,
│                                     # mistake bank — tách theo từng đợt tính năng
└── SHOP_API_POSTMAN.md, start.md, api_docs.json, postman_collection.json
                                      # tài liệu + Postman collection của nhánh feature/shop,
                                      # nằm thẳng ở root thay vì gom vào 1 thư mục docs/ (xem mục 7)
```

Để thực sự hữu ích, đây là cấu trúc **package** bên trong `src/main/java/com/example/nihongo_app/`
(cấp package, không phải cấp thư mục hệ điều hành):

```
com/example/nihongo_app/
├── controller/    (19 file)  REST endpoints (78 endpoint, đủ Swagger @Operation)
├── service/       (interface + class cụ thể, xem mục 7) +
│                  service/impl/ (business logic, gồm cả ShuffleUtil.java/CursorCodec.java dùng chung)
├── entity/        (25 file)  JPA entity
├── repository/    (Spring Data JPA)
├── dto/request/   + dto/response/   DTO vào/ra API
├── security/      (4 file)  JWT filter, provider, UserDetails
├── config/        (5 file)  Security, OpenAPI, WebMvc (static /uploads), Cache (leaderboard),
│                  MistakeReviewProperties (@ConfigurationProperties)
├── exception/     (9 file)  custom exception + GlobalExceptionHandler
└── converter/     (1 file)  JsonNodeConverter (JSON column <-> entity)
```

---

## 3. TECH STACK

| Nhóm | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework chính | Spring Boot 4.1.0 (Spring Framework 7, Spring MVC, Tomcat embedded) |
| Bảo mật | Spring Security 7.1.0 + **JWT tự viết tay** (không dùng thư viện jjwt — `JwtTokenProvider` tự code HMAC-SHA256 bằng `javax.crypto`), BCrypt cho password |
| ORM / DB access | Spring Data JPA + Hibernate ORM 7.4.1 |
| Database | MySQL 8.4 (chạy qua Docker Compose, port 3307), quản lý schema bằng **Flyway 12.4.0** (31 migration) |
| "State management" phía server | Không áp dụng theo nghĩa FE — trạng thái chủ yếu là DB (MySQL); session vẫn stateless (`SessionCreationPolicy.STATELESS`), nhưng vừa thêm **1 cache in-memory** (`spring-boot-starter-cache` + `ConcurrentMapCacheManager`) riêng cho kết quả leaderboard (`@Cacheable(value = "leaderboard", ...)`) |
| API docs | springdoc-openapi 3.1.0 (Swagger UI tại `/swagger-ui/index.html`) |
| JSON | **2 thư viện Jackson song song** (bất thường, xem mục 7): `com.fasterxml.jackson` (Spring MVC mặc định) + `tools.jackson` (Jackson 3, riêng cho `JsonNodeConverter` map cột JSON) |
| Test | JUnit 5, Mockito, AssertJ, MockMvc (springdoc/spring-boot test starter) |
| Build | Maven (`mvnw`) |
| Container hoá | Docker Compose (MySQL + phpMyAdmin), chưa có Dockerfile cho chính app |
| CI/CD | `.github/workflows/ci.yml` — build + chạy toàn bộ test trên MySQL service thật mỗi khi mở PR / push `main`. Mới thêm, chưa từng chạy thật trên GitHub Actions (chỉ verify logic tương đương ở local) |
| Thư viện khác đáng chú ý | Lombok (giảm boilerplate getter/setter/builder), `hypersistence-utils-hibernate-70` (tiện ích Hibernate) |

---

## 4. DATA MODELS

16 entity JPA (`entity/*.java`). Trích các field chính từ code thật:

### `User` — trung tâm, gộp cả identity lẫn toàn bộ gamification (không tách bảng riêng)
```java
private String email;
private String username;
private String passwordHash;
private String displayName;
private String avatarUrl;                // mới map field này (trước đây thiếu, DB có cột nhưng
                                          // entity không đọc được — nay đã fix qua UpdateAvatarRequest)
private String role;                    // "LEARNER" | "ADMIN"
// --- Gamification ---
private Integer level;
private Integer exp;
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "rank_id", nullable = false)
private Rank rank;                       // thay thế hoàn toàn enum League cũ (xem entity Rank bên dưới)
private Integer currentEnergy;
private Integer maxEnergy;
private LocalDateTime lastEnergyResetDate;
private LocalDateTime lastAdWatchDate;
private LocalDate lastChestOpenedDate;
private Integer coins;
private LocalDate lastStreakDate;
private Integer currentStreak;
private Integer longestStreak;
private Integer streakFreezeCount;
private Boolean streakFreezeAwarded;
```

### `Topic` / `Lesson` — lộ trình học
```java
// Topic
private String title;
private Integer orderIndex;
@OneToMany(mappedBy = "topic") private List<Lesson> lessons;

// Lesson
private Long topicId;
private LessonType lessonType;           // enum NORMAL | TIMED_REVIEW | JUMP_TEST
private Integer orderIndex;
@Convert(converter = JsonNodeConverter.class)
private JsonNode configJson;             // entryCostEnergy, questionsPerSession, expReward, starThresholds...
```

### `LessonQuestion` / `LessonQuestionOption`
```java
// LessonQuestion
private QuestionType questionType;       // SELECT_IMAGE, TRANSLATE_TO_JP, LISTEN_AND_ARRANGE...
private String questionText;
private String audioUrl, imageUrl;
private JsonNode metadataJson;

// LessonQuestionOption
private Long questionId;
private Boolean correct;
private Integer orderIndex;
```

### `UserLessonProgress` — tiến độ 1 user với 1 bài
```java
private Long userId, lessonId;
private ProgressStatus status;           // LOCKED | IN_PROGRESS | COMPLETED
private Integer starsEarned;
```
Trạng thái **hiển thị** thật (LOCKED/UNLOCKED/COMPLETED trên roadmap) không đọc thẳng cột này —
được tính lại mỗi lần gọi API bởi `LessonUnlockPolicy` (kết hợp `status` này + vị trí bài trong
toàn hệ thống).

### `CoinTransaction` / `UserExpLog` — log giao dịch (không phải nguồn số dư thật)
```java
// CoinTransaction: amount (âm/dương), transactionType (EARN_LESSON, BUY_ITEM, STREAK_BONUS,
// ADMIN_ADJUST, DAILY_CHEST, BUY_STREAK_FREEZE), referenceId
// UserExpLog: expGained, sourceType (NEW_LESSON, REVIEW_LESSON, JUMP_TEST), referenceId
```
Số dư thật nằm ở `User.coins`/`User.exp`; 2 bảng log này chỉ để truy vết lịch sử.

### `QuestDefinition` / `UserDailyQuest` — Daily Quest
```java
// QuestDefinition: title, questType (COMPLETE_LESSONS|CORRECT_ANSWERS|PERFECT_LESSON), targetValue, active
// UserDailyQuest: userId, questDate, targetValue, currentProgress, completed
// (title/questType/targetValue COPY từ QuestDefinition lúc gán, không tham chiếu động)
```

### `Rank` — hạng đấu (mới, thay thế enum `League` đã bị xoá hoàn toàn khỏi codebase)
```java
private String name;                     // BRONZE, SILVER, GOLD, PLATINUM, DIAMOND (seed sẵn)
private Integer minExpRequired;          // mốc EXP tối thiểu để đạt hạng
private Integer orderIndex;
```
`User.rank` trỏ thẳng tới đây (`@ManyToOne`). Thăng hạng được tính lại mỗi lần `submitLesson()`
qua `resolveHighestQualifyingRank(user.getExp())` — tìm hạng cao nhất mà `minExpRequired <= exp`.

### `ShopItem` / `UserInventory` / `UserActiveEffect` — Shop (đã qua 1 lần refactor ở `fix/shop_quantity`)
```java
// ShopItem: name, itemType (CONSUMABLE|COSMETIC|POWERUP),
//   effectType (STREAK_FREEZE|ENERGY_REFILL|DOUBLE_XP|DOUBLE_COIN|TIMER_BOOST|AVATAR_FRAME|BADGE|THEME),
//   effectValue, priceCoins, priceGems, active, limitedTime, availableFrom/Until

// UserInventory: userId, itemId, quantity (MỚI — cho phép mua/sở hữu nhiều hơn 1, trước đây thiếu
//   nên mua trùng item bị lỗi), equipped (cho cosmetic), acquiredFrom (SHOP_BUY|CHEST_REWARD|...),
//   acquiredAt — KHÔNG còn expiresAt (đã tách ra UserActiveEffect riêng bên dưới)

// UserActiveEffect (MỚI, tách khỏi UserInventory): userId, effectType, expiresAt, createdAt —
//   1 row = 1 hiệu lực powerup đang chạy (DOUBLE_XP/DOUBLE_COIN/TIMER_BOOST...), độc lập với việc
//   sở hữu bao nhiêu item trong túi đồ.
```
Powerup (`DOUBLE_XP`/`DOUBLE_COIN`/`TIMER_BOOST`) được `LessonAttemptServiceImpl.submitLesson()`
đọc qua `ShopService.hasActivePowerup(userId, effectType)` (tra `UserActiveEffect`) để nhân đôi
EXP/Coin khi đang có hiệu lực. `UserStatsResponse` (API `GET /users/me`) cũng trả kèm danh sách
`activeEffects` hiện có từ bảng này.

> **Lưu ý**: entity `ThematicSection` (mới thêm cùng đợt) có schema nhưng **không entity/DTO nào
> khác tham chiếu tới nó ngoài chính các DTO đi kèm** (`SubmitSectionRequest`, `AnswerRequest`,
> `SectionContentResponse`) — không có repository, không có service, không có controller nào dùng.
> Xem thêm mục 5c.

### `LessonAttemptAnswer` / `Mistake` — Mistake Bank (mới)
```java
// LessonAttemptAnswer: userId, lessonId, questionId, selectedOptionId,
//   isCorrect (server tự xác định bằng cách đối chiếu selectedOptionId với DB, không tin FE),
//   answeredAt — ghi lại MỌI câu trả lời (đúng lẫn sai) trong 1 lượt submit bài học thường.

// Mistake: userId, questionId (unique theo cặp), wrongCount, correctStreak,
//   status (ACTIVE|RESOLVED), lastWrongAt, lastCorrectAt
```
`Mistake` upsert theo `(userId, questionId)` — không nhân bản record mỗi lần sai. Chỉ câu SAI
trong bài `NORMAL`/`TIMED_REVIEW` (không tính `JUMP_TEST`, không tính replay) mới được ghi nhận
qua `MistakeService.recordFromAnswers()`. "Xoá nợ" (chuyển `RESOLVED`) cần đúng **2 lần trả lời
đúng cách nhau tối thiểu 1 ngày** — 2 lần đúng liên tiếp trong cùng 1 phiên/ngày không đủ điều
kiện (chống học vẹt đáp án). Sai lại bất kỳ lúc nào (kể cả khi đã `RESOLVED`) sẽ mở lại `ACTIVE`.

### `Achievement` / `UserAchievement` / `UserStreakDay` — Thành tích (mới, từ `feature/achivements`)
```java
// Achievement (định nghĩa, seed sẵn ~16 dòng): code, name, description, icon,
//   type (STREAK_MILESTONE|DAILY_STUDY|STREAK_RESET|COIN_EARNED|...), threshold, secret, active

// UserAchievement (tiến độ/unlock của 1 user với 1 achievement): userId,
//   @ManyToOne achievement, unlockedAt (null = chưa unlock), progress

// UserStreakDay: userId, studyDate, createdAt — 1 row/ngày user có học, dùng vẽ streak calendar
```
Logic nằm ở `AchievementServiceImpl.onEvent()`/`upsertProgress()`: mỗi sự kiện (học bài, streak
reset...) được đối chiếu ngưỡng (`threshold`) của từng achievement đang active; đạt ngưỡng lần đầu
mới set `unlockedAt` + (từ khi tích hợp Social Feed) tự đăng 1 bài `SYSTEM_ACHIEVEMENT` vào feed
của user đó — xem `Post` bên dưới. Achievement `secret = true` bị ẩn khỏi danh sách cho tới khi
unlock (chống spoil, vd `STREAK_365`, `COMEBACK`, `EARLY_BIRD`, `NIGHT_OWL`).

### `Character` / `UserCharacterProgress` — Bảng chữ cái (mới, từ `feature/achivements`)
```java
// Character: symbol (vd "あ"), romaji, type (HIRAGANA|KATAKANA), groupName, audioUrl,
//   strokeOrderData, orderIndex

// UserCharacterProgress: @ManyToOne user, @ManyToOne character, masteryLevel, lastPracticedAt
```
`AlphabetController` (`GET /api/v1/alphabets`, `POST .../practice/start`, `POST .../practice/submit`)
+ `AdminAlphabetController` (`POST /api/v1/admin/alphabets`, `.../bulk`) — luyện tập và chấm điểm
theo `masteryLevel`, tương tự tinh thần Duolingo cho bảng chữ cái.

### `Post` / `PostLike` / `PostComment` — Bản tin cộng đồng (Social Feed, mới)
```java
// Post: userId, content, postType (SYSTEM_ACHIEVEMENT|USER_STATUS), createdAt (DB default,
//   KHÔNG đọc lại được trong cùng transaction lúc vừa insert — xem mục 7 #15), updatedAt, deletedAt

// PostLike: userId + postId (composite PK qua @IdClass, chống like trùng ở tầng DB), createdAt

// PostComment: postId, userId, content, createdAt, updatedAt, deletedAt (soft delete)
```
Bảng `posts`/`post_likes`/`post_comments` đã có sẵn từ `V5` (schema cũ, không ai dùng) — module
Social Feed là code Java ĐẦU TIÊN ánh xạ vào các bảng này. Feed (`GET /api/v1/feed`) = bài của
chính mình + đang follow (bảng `user_follows` từ `V8`, cũng tái sử dụng nguyên vẹn), cursor-based
theo `(created_at, id)`, chống N+1 bằng 3 query gộp (like count/comment count/likedByMe) thay vì
query riêng từng post. `AchievementServiceImpl` gọi `PostService.createSystemAchievementPost()`
đúng 1 lần tại thời điểm unlock để tự động tạo bài `SYSTEM_ACHIEVEMENT`.

---

## 5. TÍNH NĂNG

### (a) Đã hoàn thành (có đủ Controller → Service → Repository, đã test)
| Tính năng | File chính |
|---|---|
| Đăng ký / đăng nhập JWT | `AuthController` → `AuthServiceImpl` → `JwtTokenProvider` |
| Roadmap + mở khoá bài tuần tự (kể cả xuyên Topic) | `RoadmapController` → `RoadmapServiceImpl` + `LessonUnlockPolicy` |
| Vòng đời làm bài (start/submit/cancel) | `LessonAttemptController` → `LessonAttemptServiceImpl` |
| Admin CRUD Topic/Lesson/Question/User | `AdminController` → `AdminContentServiceImpl` |
| Upload ảnh/audio (single + batch) | `FileController` → `FileServiceImpl`, serve tĩnh qua `WebConfig` |
| Năng lượng (hồi theo giờ, xem QC, hồi bằng coin, hoàn theo kết quả bài) | `EnergyController` → `EnergyService`, tích hợp trong `LessonAttemptServiceImpl` |
| Streak + mua Streak Freeze | `StreakController` → `StreakService` |
| Coin (faucet hoàn thành bài/mốc streak, sink mua Freeze/refill energy) | rải trong `LessonAttemptServiceImpl`, `StreakService`, `EnergyService` |
| Daily Quest (gán ngẫu nhiên, cập nhật tiến độ) | `QuestController` → `DailyQuestService` |
| Rương thưởng hàng ngày | `ChestController` → `ChestService` |
| Trạng thái tổng hợp 1 lần gọi | `UserController.getMyStats()` → `UserStatsResponse` |
| Cập nhật avatar (mới) | `UserController` → `UserServiceImpl.updateAvatarUrl()`/`getAvatarUrl()` |
| Follow / tìm kiếm user (qua native query, không có entity riêng — API cũ theo toggle/username vẫn giữ nguyên, đã mở rộng thêm idempotent follow/unfollow + hồ sơ công khai theo id + cursor pagination cho module Social Feed, xem hàng dưới) | `UserController` → `UserRepository` (`@Query nativeQuery`) |
| **Rank + Leaderboard (mới)** — thay thế hoàn toàn `League`, top 15/hạng, vị trí user hiện tại, có cache | `RankController` → `RankServiceImpl` (`@Cacheable`) |
| **Shop (mới)** — mua vật phẩm bằng coin, túi đồ, kích hoạt powerup (Double XP/Coin, Timer Boost), trang bị cosmetic | `ShopController` → `ShopService` |
| **Mistake Bank** — ghi lại từng câu trả lời khi submit bài (server tự chấm từ DB), tự động đưa câu sai vào "ngân hàng lỗi sai", phiên ôn tập riêng (không trừ năng lượng, không lộ đáp án, thưởng năng lượng giới hạn/ngày, không cộng EXP/Coin) | `MistakeReviewController` → `MistakeService`, tích hợp trong `LessonAttemptServiceImpl.submitLesson()` |
| **Achievements (mới)** — unlock thành tích theo ngưỡng/sự kiện (streak, học bài, giờ học sớm/muộn, comeback sau nghỉ...), ẩn achievement `secret` cho tới khi unlock, streak calendar | `AchievementController` → `AchievementServiceImpl` |
| **Bảng chữ cái (mới)** — luyện Hiragana/Katakana, chấm điểm theo `masteryLevel` | `AlphabetController`/`AdminAlphabetController` → `AlphabetServiceImpl` |
| **Bản tin cộng đồng / Social Feed (mới)** — follow 1 chiều (idempotent), feed tổng hợp cursor-pagination (chống N+1), đăng trạng thái, like, comment, tự đăng bài khi unlock thành tích | `FeedController`/`PostController`/`CommentController` → `FeedServiceImpl`/`PostServiceImpl`; mở rộng `UserController`/`UserServiceImpl` cho follow/hồ sơ công khai |
| Swagger/OpenAPI cho toàn bộ 78 endpoint | `OpenApiConfig` + `@Tag`/`@Operation` trên mọi controller |

### (b) Đang làm dở / nửa vời (có field, có infra, nhưng thiếu logic nghiệp vụ)
- **`User.level` không bao giờ tăng**: grep toàn repo, `setLevel(...)` **không được gọi ở bất kỳ
  đâu** ngoài giá trị khởi tạo `1`. Có `exp` cộng dồn liên tục và giờ đã dùng để tính **Rank**
  (mục 4), nhưng field `level` riêng thì vẫn đứng yên — 2 khái niệm "level" và "rank" hiện tồn tại
  song song, chỉ 1 trong 2 thật sự hoạt động.
- ~~`User.currentLeague` không bao giờ đổi~~ / ~~Weekly Leaderboard chưa tồn tại~~ — **đã được
  giải quyết**: nhánh `kiet1` vừa merge thay thế hoàn toàn League bằng hệ `Rank` + `RankController`
  có leaderboard thật (top 15, vị trí user, cache) — xem mục 5a và mục 4.
- ~~Avatar user không map trong entity~~ — **đã được fix**: `User.avatarUrl` giờ đã map đúng, có
  `UpdateAvatarRequest`/`AvatarUrlResponse` (mục 5a).

### (c) Mới là placeholder (có schema/entity nhưng chưa có nghiệp vụ hoặc chưa nối tới controller)
- **Thi thử JLPT** — bảng `exams`, `exam_sections`, `questions`, `exam_results`,
  `user_exam_answers` (từ `V6__create_exam_tables.sql`, đã hỗ trợ sẵn N5-N1, chấm điểm theo
  section, lưu đáp án từng câu) — không có entity/repository/service/controller nào.
- ~~Mạng xã hội nội bộ (bảng `posts`/`post_likes`/`post_comments` từ V5, 0 dòng code Java)~~ —
  **đã có code thật**: module Bản tin cộng đồng (Social Feed) vừa map đầy đủ vào các bảng này, xem
  mục 4/5a. Migration `V35__remove_conversation_feature.sql` cũng đã dọn 1 tính năng "Conversation"
  từng bị áp thẳng lên DB dev chung mà không có trong git ở bất kỳ nhánh nào (phát hiện lúc merge
  `fix/shop_quantity`) — không liên quan Social Feed hiện tại, chỉ là dọn rác lịch sử.
- **Gacha/vật phẩm kiểu cũ** — bảng gốc `gacha_items`, `inventory_transactions` (từ `V4`, thời
  hearts/REFILL_HEARTS trước khi đổi sang energy). Bảng `user_inventories` cùng đợt đã được
  **tái sử dụng thật** bởi hệ Shop mới (entity `UserInventory`), nhưng riêng `gacha_items` thì vẫn
  mồ côi — không entity nào ánh xạ tới.
- **`ThematicSection`** (mới, từ nhánh `feature/shop`) — có entity + 4 DTO đi kèm
  (`SubmitSectionRequest`, `AnswerRequest`, `SectionContentResponse`, `SubmitSectionResponse`)
  nhưng **không có repository, service hay controller nào dùng tới** — y hệt mẫu hình Exam/Social
  ở trên: được thiết kế trước, chưa kịp nối nghiệp vụ.

---

## 6. LUỒNG CHÍNH — 1 lượt làm bài học từ đầu đến cuối

```
1. FE gọi GET /api/v1/topics
   RoadmapController → RoadmapServiceImpl.getRoadmap()
   → TopicRepository.findAllActiveWithLessons() (1 query JOIN FETCH)
   → LessonUnlockPolicy.computeStatuses() tính LOCKED/UNLOCKED/COMPLETED cho MỌI bài
     (carry cờ "bài NORMAL trước đã xong" xuyên suốt các Topic theo thứ tự orderIndex)
   → trả về danh sách Topic + Lesson kèm status, FE tô màu khoá/mở trên bản đồ.

2. User bấm vào 1 bài UNLOCKED -> FE gọi POST /api/v1/lessons/{id}/start
   LessonAttemptController → LessonAttemptServiceImpl.startLesson()
   a. LessonUnlockPolicy.evaluate() check lại 1 lần nữa (không tin FE) -> LOCKED thì 403.
   b. Nếu bài đã COMPLETED trước đó -> isReplay=true, KHÔNG trừ năng lượng.
      Ngược lại: resolveEntryCost() (mặc định 10, hoặc configJson.entryCostEnergy riêng) ->
      kiểm tra đủ energy -> trừ -> User.setCurrentEnergy() -> save.
   c. Upsert UserLessonProgress = IN_PROGRESS.
   d. Lấy toàn bộ LessonQuestion của bài, shuffle (Fisher-Yates), cắt lấy N câu
      (configJson.questionsPerSession, mặc định 10), shuffle luôn thứ tự đáp án.
   e. Trả về đề thi kèm ĐÁP ÁN ĐÚNG luôn trong response (option.isCorrect) -> FE tự chấm
      real-time phía client, BE chỉ verify lại tổng kết ở bước submit.

3. User làm bài trên FE (BE không biết gì về tiến trình từng câu).

4. FE gọi POST /api/v1/lessons/{id}/submit với tổng kết
   (totalQuestions, totalCorrect, totalMistakes, timeTakenSeconds?, heartsRemaining?, isReplay?)
   LessonAttemptServiceImpl.submitLesson():
   a. Kiểm tra powerup Shop đang có hiệu lực (ShopService.hasActivePowerup: DOUBLE_XP/DOUBLE_COIN/
      TIMER_BOOST) TRƯỚC khi tính thưởng, để áp nhân đôi nếu có.
   b. Tính expGained/coinsGained theo lessonType (switch NORMAL/TIMED_REVIEW/JUMP_TEST),
      nhân tỉ lệ replay (mặc định 30%) nếu isReplay=true, rồi nhân đôi nếu có powerup DOUBLE_XP/
      DOUBLE_COIN đang active.
   c. Upsert UserLessonProgress -> COMPLETED (JUMP_TEST pass còn đánh dấu COMPLETED luôn
      mọi bài NORMAL/TIMED_REVIEW còn lại trong Topic - cơ chế "nhảy cóc").
   d. Cộng EXP vào User.exp + ghi UserExpLog.
   e. Nếu passed: cộng Coin + ghi CoinTransaction; hoàn 1 phần Energy tuỳ kết quả (hoàn hảo
      +5 / khá +2 / còn lại +0, cap ở đúng số đã trừ lúc start để chặn exploit replay);
      cập nhật tiến độ Daily Quest (DailyQuestService.recordProgress); gọi
      StreakService.checkAndUpdateStreak() rồi cộng thêm Coin nếu vừa chạm mốc 7/30/100 ngày.
   f. Tính lại Rank cao nhất user đủ điều kiện theo EXP mới (resolveHighestQualifyingRank) — nếu
      khác Rank hiện tại thì cập nhật User.rank và đánh dấu isPromoted=true.
   g. (Mistake Bank, optional) Nếu FE có gửi req.answers (list {questionId, selectedOptionId}):
      tự chấm từng câu bằng DB (KHÔNG tin isCorrect tu FE vì field nay khong ton tai), luu vao
      lesson_attempt_answers. Voi cau SAI trong bai NORMAL/TIMED_REVIEW (khong tinh JUMP_TEST,
      khong tinh replay) -> upsert vao Mistake Bank (bang user_mistakes).
   h. Trả SubmitLessonResponse (status, expEarned, coinsEarned, starsEarned, isTopicCompleted,
      isPromoted, newRankName, currentEnergy) -> FE cập nhật UI, có thể gọi thêm GET quests/chest.

   (Nhánh phụ - Mistake Bank: FE có thể gọi rieng POST /api/v1/reviews/mistakes/start de lay toi
   da 10 cau dang sai nhieu nhat/gan nhat ra on (khong tru energy, khong lo dap an), roi
   POST .../submit de nop - dung 2 lan cach nhau >=1 ngay moi "xoa no" 1 cau, thuong +5 energy/
   phien hop le toi da 2 phien/ngay, khong cong EXP/Coin.)

   (Nhánh phụ: nếu user back giữa chừng chưa submit -> POST /cancel hoàn lại đúng số
   năng lượng đã trừ lúc start, dựa trên UserLessonProgress đang IN_PROGRESS.)
```

---

## 7. VẤN ĐỀ (tech debt, hardcode, trùng lặp)

**Không tìm thấy comment `TODO`/`FIXME`/`HACK` nào trong `src/`** — nhưng có các vấn đề thực chất
sau, phát hiện qua đọc code:

1. ~~JWT secret hardcode cứng, không có cách override qua biến môi trường~~ — **đã fix**: đọc qua
   `${JWT_SECRET:...}` (giữ nguyên giá trị dev cũ làm default để không làm vô hiệu token đang có),
   production giờ override được. Đồng thời phát hiện thêm 1 vấn đề còn nghiêm trọng hơn lúc sửa —
   **mật khẩu MySQL thật bị hardcode làm default của `DB_PASSWORD`** (không phải placeholder) —
   cũng đã fix, thay bằng giá trị dev mặc định khớp `docker-compose.yml`. Cả 2 file từng lộ mật
   khẩu này (`application.yml`, `start.md`) đã được dọn sạch — chỉ còn tồn tại trong lịch sử git
   (nên cân nhắc đổi mật khẩu MySQL đó nếu repo là public hoặc từng push lên xa).

2. **N+1 query trong `AdminContentServiceImpl`**
   `toTopicWithLessonsResponse()` và `toLessonWithQuestionsResponse()` gọi
   `questionRepository.findAllByLessonIdOrderByIdAsc(...)` / `optionRepository.findAll...` **bên
   trong** `.stream().map()` — với N lesson/question sẽ ra N+1 câu query. Chấp nhận được ở quy mô
   admin hiện tại nhưng sẽ chậm dần khi nội dung tăng.

3. **Comment lạ sót lại trong code** — `AdminContentServiceImpl.java` dòng cuối:
   `} // Chú ý giữ lại dấu ngoặc nhọn đóng của class ở cuối cùng nhé` — đọc như ghi chú của 1
   phiên AI-assist trước để lại, không phải comment nghiệp vụ. Nên dọn.

4. ~~Đường dẫn upload hardcode tuyệt đối kiểu Windows làm default~~ — **đã fix**: default đổi sang
   đường dẫn tương đối `./uploads`, không còn phụ thuộc OS. `FileServiceImpl` cũng đã thêm
   `@PostConstruct` tạo thư mục lúc khởi động thay vì đợi tới lần upload đầu tiên.

5. **Trùng cấu hình max file size** — vừa khai ở `application.yml`
   (`spring.servlet.multipart.max-file-size: 5MB`) vừa hardcode lại bằng Java constant
   `FileServiceImpl.MAX_FILE_SIZE = 5 * 1024 * 1024` — 2 nơi phải sửa cùng lúc nếu đổi giới hạn,
   dễ lệch nhau.

6. **2 hệ Jackson song song trong cùng 1 project** — `com.fasterxml.jackson` (mặc định của Spring
   MVC) và `tools.jackson` (Jackson 3, chỉ dùng cho `JsonNodeConverter`). Hoạt động được nhưng là
   nguồn nhầm lẫn import (`JsonNode` có 2 class cùng tên khác package).

7. **Đáp án đúng trả thẳng về client lúc `/start`** (`StartLessonOption.isCorrect`) — thiết kế cố
   ý (comment trong code xác nhận: "BE chỉ verify lại ở /submit khi cần thiết"), nhưng đồng nghĩa
   user có thể inspect network traffic để thấy đáp án trước khi làm — chấp nhận được cho use-case
   hiện tại (BE không thật sự "verify lại" ở submit, chỉ tin số liệu FE tự gửi), nhưng là điểm
   yếu nếu sau này cần chống gian lận chặt hơn.

8. **`getAllQuestions()` trong `AdminContentServiceImpl` thiếu `@Transactional(readOnly = true)`**
   — mọi method đọc khác trong cùng class đều có, riêng method này không, không nhất quán.

9. **File `package-lock.json` rỗng, thừa** ở root — không phục vụ mục đích gì cho 1 project
   Maven thuần, nên xoá để tránh gây hiểu nhầm khi người mới join đọc repo.

10. ~~Không có CI/CD thật~~ — **đã thêm** `.github/workflows/ci.yml` (build + test trên MySQL
    service thật, chạy khi mở PR / push `main`). ⚠️ Nhưng **chưa từng chạy thật trên GitHub
    Actions** — mới verify logic tương đương (cùng bộ biến môi trường) ở máy local, nên vẫn có
    rủi ro sai sót đặc thù môi trường CI (network giữa job và service container, quyền thực thi
    `mvnw` trên Linux runner...) chưa được kiểm chứng. File này do 1 extension VS Code
    "app modernization" tự sinh trong lúc làm việc (không phải code viết tay từ đầu), đã review
    và sửa 1 bug (`DB_URL` không khớp property thật) trước khi commit.

11. **1/3 các "service" không theo pattern interface + impl** (đỡ hơn trước — tỉ lệ đang cải thiện
    dần) — quy ước codebase đang dùng là tách `XxxService` (interface, trong `service/`) +
    `XxxServiceImpl` (class, trong `service/impl/`) — 12 service theo đúng: `AdminContentService`,
    `AuthService`, `FileService`, `LessonAttemptService`, `MistakeService`, `RankService`,
    `RoadmapService`, `UserService`, và 4 service mới thêm gần đây đều theo đúng chuẩn ngay từ đầu
    (`AchievementService`, `AlphabetService`, `FeedService`, `PostService`). Nhưng **6 service vẫn
    là 1 class cụ thể nằm thẳng trong `service/`, không có interface** (không đổi so với trước):
    `ChestService`, `DailyQuestService`, `EnergyService`, `LessonUnlockPolicy`, `ShopService`,
    `StreakService` — không phải bug (build/test vẫn chạy tốt), nhưng khó mock/thay thế khi cần,
    nên cân nhắc thống nhất về 1 kiểu nếu có dịp refactor lớn 6 file còn lại này.

12. **File "rác" lọt vào commit khi merge nhánh đồng đội (đã dọn ở lần merge này)** — nhánh `kiet1`
    commit nhầm 6 file log build (`app.log`, `mvn-*.log`), đã xoá khỏi git và thêm `*.log` vào
    `.gitignore` để chặn tái diễn. Nhánh `feature/shop` mang theo `.claude/settings.json` (config
    Claude Code cá nhân của đồng đội), `api_docs.json`, `postman_collection.json`,
    `SHOP_API_POSTMAN.md`, `start.md` nằm thẳng ở root thay vì gom vào 1 thư mục `docs/` — giữ lại
    vì không hại gì (không chứa secret), nhưng nên dọn vào `docs/` sau. Đây là lần thứ 2 kiểu file
    rác này xuất hiện (lần đầu ở nhánh `feature/energy_recovery` cũ, ~12 file `*.js` debug — nhánh
    đó không được merge vào nên không ảnh hưởng) — team nên tập thói quen `git status` trước khi
    commit.

13. **`exp` là từ khoá dành riêng trong grammar HQL của Hibernate 6/7 (đã fix, nhưng đáng ghi chú
    lại làm bài học)** — 2 query JPQL mới của nhánh `kiet1` (`UserRepository`, cho Leaderboard)
    viết `u.exp` bị lỗi `no viable alternative at input 'u.exp'` — `exp` trùng tên hàm toán học
    dựng sẵn (`EXP()`). Escape bằng backtick (cú pháp Hibernate tài liệu chính thức có nhắc tới)
    **không hoạt động** trong bản Hibernate 7.4.1 đang dùng; đã chuyển 2 query đó sang native SQL
    để tránh hẳn HQL parser. **Lưu ý cho code sau này**: tránh viết `u.exp`/`entity.exp` (hay các
    tên hàm SQL/HQL khác: `sum`, `avg`, `count`, `abs`, `sign`, `mod`, `sqrt`, `ln`, `power`,
    `year`, `month`, `day`...) trực tiếp trong chuỗi `@Query` JPQL nếu entity có field trùng tên.

14. **Một vài file tự nhiên biến mất / xuất hiện lại không rõ lý do trong lúc làm việc** — gặp 2
    lần: file `DATABASE_SETUP.md` (mới tạo, "File created successfully") biến mất hoàn toàn khỏi
    ổ đĩa ngay sau đó; và file `entity/League.java` (đã bị xoá thật từ lúc merge `kiet1`, xác nhận
    qua `git show HEAD` không có) tự xuất hiện lại trên ổ đĩa dưới dạng file **chưa track** (kèm 1
    file rỗng `.github/workflows/a`). Nghi do đồng bộ OneDrive (thư mục project nằm trong
    `OneDrive/Documents/...`) có xung đột/khôi phục phiên bản cũ ngầm — không ảnh hưởng git/build
    (đều là file chưa từng commit hoặc đã tạo lại được), nhưng nên lưu ý nếu thấy hiện tượng tương
    tự: kiểm tra `git status` trước khi hoảng, và cân nhắc tạm dừng đồng bộ OneDrive lúc code nếu
    lặp lại nhiều.

15. **`createdAt`/`updatedAt` (insertable=false) có thể đọc ra `null` nếu tạo entity rồi đọc lại
    NGAY trong cùng 1 transaction/persistence context** — phát hiện lúc viết test cho `Post`
    (module Social Feed). Nguyên nhân: cột này để DB tự set qua `DEFAULT CURRENT_TIMESTAMP`,
    Hibernate không tự SELECT lại giá trị đó vào object Java sau INSERT (không có
    `@Generated`/`@CreationTimestamp`); nếu ngay sau đó có 1 query khác (kể cả native query) trả về
    đúng row vừa tạo, Hibernate ưu tiên trả **object đang quản lý trong session** (vẫn `createdAt =
    null`) thay vì dựng lại object mới từ ResultSet. **Không phải bug thực tế** vì mỗi HTTP request
    thật có persistence context riêng (OSIV bật sẵn) nên tạo + đọc luôn là 2 request khác nhau — chỉ
    lộ ra trong test dùng `@Transactional` để gom nhiều lệnh `MockMvc` vào chung 1 transaction.
    Đây là hành vi **giống hệt mọi entity khác trong codebase** (không riêng `Post`) vì không entity
    nào dùng `@CreationTimestamp`/`@Generated` — chỉ cần lưu ý nếu sau này có API "trả lại entity
    vừa tạo trong cùng response" thì phải set thủ công field này (không dựa vào DB default).

16. **Đã xác nhận (không phải giả định) `ResponseStatusException` đi đúng qua
    `GlobalExceptionHandler`** — `GlobalExceptionHandler extends ResponseEntityExceptionHandler` có
    1 handler catch-all `@ExceptionHandler(Exception.class)` trông như có thể "nuốt" nhầm mọi lỗi
    nghiệp vụ thành 500. Đã soi hierarchy thật trong `spring-web-7.0.8.jar`:
    `ResponseStatusException extends ErrorResponseException` (1 bước), còn `Exception.class` (handler
    catch-all) cách xa hơn (4 bước) — nên `ResponseEntityExceptionHandler.handleException(...)` (đã
    khai `ErrorResponseException.class`) luôn thắng, trả đúng status (400/403/404/409) mã hoá trong
    exception. Đã verify thêm bằng test MockMvc thật (`SocialFeedIntegrationTest`) chứ không chỉ suy
    luận lý thuyết — an toàn để tiếp tục dùng pattern `throw new ResponseStatusException(...)` cho
    lỗi nghiệp vụ đơn giản mà không cần viết custom exception class riêng.

---

## 8. SỐ LIỆU

| Thành phần | Số lượng |
|---|---|
| File `.java` (main) | **189** |
| File `.java` (test) | **18** |
| File migration Flyway (`.sql`) | **37** |
| Tổng số dòng code Java (main) | **10.721 dòng** |
| Tổng số dòng code Java (test) | **3.336 dòng** |
| Tổng số dòng SQL migration | **894 dòng** |
| Số commit trên nhánh hiện tại | **54** |

**Số dòng theo thư mục (main, ước tính bằng `wc -l`):**

| Thư mục | Dòng |
|---|---|
| `service/impl/` | 3.117 |
| `dto/` (request + response) | 1.606 |
| `entity/` | 1.572 |
| `controller/` | 1.564 |
| `service/` (19 file: 12 interface + 6 class cụ thể + 1 DTO `AchievementProgress`, xem mục 7) | 1.412 |
| `repository/` | 680 |
| `security/` | 269 |
| `exception/` | 272 |
| `config/` | 182 |
| `converter/` | 34 |

**Số entity**: 25 (16 trước đó, đã gồm sẵn `Rank`/`ShopItem`/`ThematicSection`/`UserInventory`/
`LessonAttemptAnswer`/`Mistake`, + 9 entity mới: `Achievement`, `UserAchievement`,
`UserStreakDay`, `Character`, `UserCharacterProgress`, `UserActiveEffect`, `Post`, `PostLike`,
`PostComment`) · **Số controller**: 19 (78 endpoint, đủ Swagger
`@Operation`) · **Số Flyway migration**: 37 (V1 → V37; `V32`/`V33` từng đổi số 2 lần lúc merge
song song `feature/achivements` + `fix/shop_quantity`; `V35` dọn 1 tính năng "Conversation" từng bị
áp thẳng lên DB dev mà không có trong git; `V37` thêm index cho Social Feed) · **Bảng DB có
migration nhưng chưa có entity Java dùng thật**: 7 — `gacha_items`, `inventory_transactions` (từ
`V4`; bảng `user_inventories` cùng đợt thì đã được hệ Shop tái sử dụng qua entity `UserInventory`),
`exams`, `exam_sections`, `questions`, `exam_results`, `user_exam_answers`
(`V6`) — xem mục 5c.
