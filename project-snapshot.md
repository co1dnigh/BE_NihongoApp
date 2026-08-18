# Project Snapshot — BE_NihongoApp

> Snapshot dựa trên phân tích code thực tế tại thời điểm viết (nhánh `feature/coin-reward-chest`,
> sau khi merge thêm `feature/shop` và `kiet1` — 29 commit, 124 file Java main + 11 file test,
> 28 migration Flyway). Không có API key/token/mật khẩu thật nào bị lộ trong tài liệu này — mọi
> chỗ có giá trị nhạy cảm đều bị che theo yêu cầu.

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
- Hệ gamification đầy đủ: EXP/Level/**Rank** (hạng đấu kèm Leaderboard — vừa thay thế hệ `League`
  cũ), Streak (chuỗi ngày học), Energy (năng lượng giới hạn số bài học/ngày), Coin, Daily Quest,
  Rương thưởng (Chest), **Shop** (mua vật phẩm/powerup bằng coin, có túi đồ riêng).
- Quản trị nội dung (Admin CRUD Topic/Lesson/Question), xác thực JWT, upload file, tìm
  bạn/follow.
- Có sẵn schema DB (nhưng **chưa có code**) cho thi thử JLPT và mạng xã hội nội bộ; ngoài ra có
  1 entity mới (`ThematicSection`) cũng chưa nối vào controller/service nào (xem mục 5c).

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
├── .github/modernize/java-upgrade/   # tooling hook (không phải CI/CD workflow thật)
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
└── SHOP_API_POSTMAN.md, start.md, api_docs.json, postman_collection.json
                                      # tài liệu + Postman collection của nhánh feature/shop,
                                      # nằm thẳng ở root thay vì gom vào 1 thư mục docs/ (xem mục 7)
```

Để thực sự hữu ích, đây là cấu trúc **package** bên trong `src/main/java/com/example/nihongo_app/`
(cấp package, không phải cấp thư mục hệ điều hành):

```
com/example/nihongo_app/
├── controller/    (12 file)  REST endpoints
├── service/       (13 file: 12 interface + 1 ngoại lệ là class cụ thể, xem mục 7) +
│                  service/impl/ (7 file)  business logic
├── entity/        (14 file)  JPA entity
├── repository/    (13 file)  Spring Data JPA
├── dto/request/   + dto/response/   DTO vào/ra API
├── security/      (4 file)  JWT filter, provider, UserDetails
├── config/        (4 file)  Security, OpenAPI, WebMvc (static /uploads), Cache (leaderboard)
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
| Database | MySQL 8.4 (chạy qua Docker Compose), quản lý schema bằng **Flyway 12.4.0** (28 migration) |
| "State management" phía server | Không áp dụng theo nghĩa FE — trạng thái chủ yếu là DB (MySQL); session vẫn stateless (`SessionCreationPolicy.STATELESS`), nhưng vừa thêm **1 cache in-memory** (`spring-boot-starter-cache` + `ConcurrentMapCacheManager`) riêng cho kết quả leaderboard (`@Cacheable(value = "leaderboard", ...)`) |
| API docs | springdoc-openapi 3.1.0 (Swagger UI tại `/swagger-ui/index.html`) |
| JSON | **2 thư viện Jackson song song** (bất thường, xem mục 7): `com.fasterxml.jackson` (Spring MVC mặc định) + `tools.jackson` (Jackson 3, riêng cho `JsonNodeConverter` map cột JSON) |
| Test | JUnit 5, Mockito, AssertJ, MockMvc (springdoc/spring-boot test starter) |
| Build | Maven (`mvnw`) |
| Container hoá | Docker Compose (MySQL + phpMyAdmin), chưa có Dockerfile cho chính app |
| Thư viện khác đáng chú ý | Lombok (giảm boilerplate getter/setter/builder), `hypersistence-utils-hibernate-70` (tiện ích Hibernate) |

---

## 4. DATA MODELS

14 entity JPA (`entity/*.java`). Trích các field chính từ code thật:

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

### `ShopItem` / `UserInventory` — Shop (mới)
```java
// ShopItem: name, itemType (CONSUMABLE|COSMETIC|POWERUP),
//   effectType (STREAK_FREEZE|ENERGY_REFILL|DOUBLE_XP|DOUBLE_COIN|TIMER_BOOST|AVATAR_FRAME|BADGE|THEME),
//   effectValue, priceCoins, priceGems, active, limitedTime, availableFrom/Until

// UserInventory: userId, shopItemId, expiresAt (cho powerup có thời hạn),
//   equipped (cho cosmetic), acquiredFrom (SHOP_BUY|CHEST_REWARD|QUEST_REWARD|...), acquiredAt
```
Powerup (`DOUBLE_XP`/`DOUBLE_COIN`/`TIMER_BOOST`) được `LessonAttemptServiceImpl.submitLesson()`
đọc qua `ShopService.hasActivePowerup(userId, effectType)` để nhân đôi EXP/Coin khi đang có hiệu lực.

> **Lưu ý**: entity `ThematicSection` (mới thêm cùng đợt) có schema nhưng **không entity/DTO nào
> khác tham chiếu tới nó ngoài chính các DTO đi kèm** (`SubmitSectionRequest`, `AnswerRequest`,
> `SectionContentResponse`) — không có repository, không có service, không có controller nào dùng.
> Xem thêm mục 5c.

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
| Follow / tìm kiếm user (qua native query, không có entity riêng) | `UserController` → `UserRepository` (`@Query nativeQuery`) |
| **Rank + Leaderboard (mới)** — thay thế hoàn toàn `League`, top 15/hạng, vị trí user hiện tại, có cache | `RankController` → `RankServiceImpl` (`@Cacheable`) |
| **Shop (mới)** — mua vật phẩm bằng coin, túi đồ, kích hoạt powerup (Double XP/Coin, Timer Boost), trang bị cosmetic | `ShopController` → `ShopService` |
| Swagger/OpenAPI cho toàn bộ 55 endpoint | `OpenApiConfig` + `@Tag`/`@Operation` trên mọi controller |

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
- **Mạng xã hội nội bộ** — bảng `posts`, `post_likes`, `post_comments` (từ
  `V5__create_social_tables.sql`, hỗ trợ `SYSTEM_ACHIEVEMENT`/`USER_STATUS`) — tương tự, 0 dòng
  code Java.
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
   g. Trả SubmitLessonResponse (status, expEarned, coinsEarned, starsEarned, isTopicCompleted,
      isPromoted, newRankName, currentEnergy) -> FE cập nhật UI, có thể gọi thêm GET quests/chest.

   (Nhánh phụ: nếu user back giữa chừng chưa submit -> POST /cancel hoàn lại đúng số
   năng lượng đã trừ lúc start, dựa trên UserLessonProgress đang IN_PROGRESS.)
```

---

## 7. VẤN ĐỀ (tech debt, hardcode, trùng lặp)

**Không tìm thấy comment `TODO`/`FIXME`/`HACK` nào trong `src/`** — nhưng có các vấn đề thực chất
sau, phát hiện qua đọc code:

1. **JWT secret hardcode cứng, KHÔNG có cách override qua biến môi trường**
   `application.yml`: `app.jwt.secret: [đã ẩn - có tồn tại trong file application.yml]` — khác với
   `DB_USERNAME`/`DB_PASSWORD` (đều dùng cú pháp `${ENV_VAR:default}`), dòng này là **giá trị
   literal**, không có `${JWT_SECRET:...}`. Nếu deploy production mà quên sửa tay, mọi môi trường
   sẽ ký JWT bằng đúng 1 chuỗi y hệt nhau (public trên git) — rủi ro bảo mật thật, không chỉ là
   "để trống lúc dev".

2. **N+1 query trong `AdminContentServiceImpl`**
   `toTopicWithLessonsResponse()` và `toLessonWithQuestionsResponse()` gọi
   `questionRepository.findAllByLessonIdOrderByIdAsc(...)` / `optionRepository.findAll...` **bên
   trong** `.stream().map()` — với N lesson/question sẽ ra N+1 câu query. Chấp nhận được ở quy mô
   admin hiện tại nhưng sẽ chậm dần khi nội dung tăng.

3. **Comment lạ sót lại trong code** — `AdminContentServiceImpl.java` dòng cuối:
   `} // Chú ý giữ lại dấu ngoặc nhọn đóng của class ở cuối cùng nhé` — đọc như ghi chú của 1
   phiên AI-assist trước để lại, không phải comment nghiệp vụ. Nên dọn.

4. **Đường dẫn upload hardcode tuyệt đối kiểu Windows làm default**
   `application.yml`: `upload-dir: ${UPLOAD_DIR:D:/Project/nihongo-app/uploads}` — default là 1
   đường dẫn Windows tuyệt đối, sẽ lỗi ngay nếu chạy trên Linux/VPS mà quên set `UPLOAD_DIR`.

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

10. **Không có CI/CD thật** — `.github/` chỉ có hook script của tool "java-upgrade" (có vẻ do
    GitHub Copilot/modernize tooling tạo), không có `.github/workflows/*.yml` nào chạy build/test
    tự động khi push/PR.

11. **`ShopService` không theo pattern interface + impl của phần còn lại codebase** — mọi service
    khác đều tách `XxxService` (interface) + `XxxServiceImpl` (class, nằm trong `service/impl/`):
    `DailyQuestService`, `ChestService`, `StreakService`, `RankService`... Riêng `ShopService`
    (từ nhánh `feature/shop`) là **1 class cụ thể** (`public class ShopService`, có `@Service`)
    nằm thẳng trong `service/` — 302 dòng logic nghiệp vụ không có interface để mock/thay thế.
    Không phải bug (build/test vẫn chạy tốt), nhưng lệch chuẩn thiết kế đang dùng, nên cân nhắc
    tách `ShopService` (interface) + `ShopServiceImpl` cho nhất quán nếu có dịp refactor.

12. **File "rác" lọt vào commit khi merge nhánh đồng đội (đã dọn ở lần merge này)** — nhánh `kiet1`
    commit nhầm 6 file log build (`app.log`, `mvn-*.log`), đã xoá khỏi git và thêm `*.log` vào
    `.gitignore` để chặn tái diễn. Nhánh `feature/shop` mang theo `.claude/settings.json` (config
    Claude Code cá nhân của đồng đội), `api_docs.json`, `postman_collection.json`,
    `SHOP_API_POSTMAN.md`, `start.md` nằm thẳng ở root thay vì gom vào 1 thư mục `docs/` — giữ lại
    vì không hại gì (không chứa secret), nhưng nên dọn vào `docs/` sau. Đây là lần thứ 2 kiểu file
    rác này xuất hiện (lần đầu ở nhánh `feature/energy_recovery` cũ, ~12 file `*.js` debug — nhánh
    đó không được merge vào nên không ảnh hưởng) — team nên tập thói quen `git status` trước khi
    commit.

---

## 8. SỐ LIỆU

| Thành phần | Số lượng |
|---|---|
| File `.java` (main) | **124** |
| File `.java` (test) | **11** |
| File migration Flyway (`.sql`) | **28** |
| Tổng số dòng code Java (main) | **7.083 dòng** |
| Tổng số dòng code Java (test) | **1.902 dòng** |
| Tổng số dòng SQL migration | **682 dòng** |
| Số commit trên nhánh hiện tại | **29** |

**Số dòng theo thư mục (main, ước tính bằng `wc -l`):**

| Thư mục | Dòng |
|---|---|
| `service/impl/` | 1.761 |
| `dto/` (request + response) | 1.130 |
| `controller/` | 1.060 |
| `service/` (interface + 1 class ngoại lệ, xem mục 7) | 1.123 |
| `entity/` | 965 |
| `security/` | 269 |
| `exception/` | 272 |
| `repository/` | 306 |
| `config/` | 150 |
| `converter/` | 34 |

**Số entity**: 14 (11 gốc + `Rank`, `ShopItem`, `ThematicSection`, `UserInventory` − `League` đã
xoá) · **Số controller**: 12 (55 endpoint, xem Swagger) · **Số Flyway migration**: 28 (V1 → V28,
2 migration cuối `V27__ranks.sql`/`V28__drop_current_league.sql` vừa được đổi số từ V24/V25 lúc
merge để tránh trùng với `V24-V26` của nhánh shop) · **Bảng DB có migration nhưng chưa có entity
Java dùng thật**: 10 — `gacha_items`, `inventory_transactions` (từ `V4`; bảng `user_inventories`
cùng đợt thì đã được hệ Shop mới tái sử dụng qua entity `UserInventory`), `posts`, `post_likes`,
`post_comments` (`V5`), `exams`, `exam_sections`, `questions`, `exam_results`, `user_exam_answers`
(`V6`) — xem mục 5c.
