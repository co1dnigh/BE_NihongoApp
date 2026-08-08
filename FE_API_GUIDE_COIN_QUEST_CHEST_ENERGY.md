# FE API Guide — Coin / Daily Quest / Rương thưởng / Năng lượng

> Tài liệu này liệt kê **API mới thêm** và **API đã đổi hành vi** trên nhánh
> `feature/coin-reward-chest`, để FE tích hợp mà không cần đọc code backend. Toàn bộ API còn lại
> (login, roadmap, admin...) không đổi — xem đầy đủ + thử trực tiếp tại **Swagger UI**:
> `http://localhost:8090/swagger-ui/index.html` (spec JSON: `/v3/api-docs`).

## Mục lục
1. [Xác thực chung](#1-xác-thực-chung)
2. [API mới: Trạng thái tổng hợp (GET /users/me)](#2-api-mới-trạng-thái-tổng-hợp-get-usersme)
3. [Format lỗi chung](#3-format-lỗi-chung)
4. [API mới: Daily Quest](#4-api-mới-daily-quest)
5. [API mới: Rương thưởng (Chest)](#5-api-mới-rương-thưởng-chest)
6. [API mới: Mua Streak Freeze](#6-api-mới-mua-streak-freeze)
7. [API đổi: Năng lượng (Energy)](#7-api-đổi-năng-lượng-energy)
8. [API đổi: Nộp bài học (Submit Lesson)](#8-api-đổi-nộp-bài-học-submit-lesson)
9. [Bảng tổng hợp lỗi nghiệp vụ cần FE xử lý riêng](#9-bảng-tổng-hợp-lỗi-nghiệp-vụ-cần-fe-xử-lý-riêng)

---

## 1. Xác thực chung

Tất cả API dưới đây (trừ khi ghi chú khác) yêu cầu header:

```
Authorization: Bearer <accessToken>
```

`accessToken` lấy từ `POST /api/v1/auth/login` (không đổi trong lần cập nhật này).

---

## 2. API mới: Trạng thái tổng hợp (`GET /users/me`)

> 🎯 Đây là API giải quyết đúng vấn đề FE từng báo: các số liệu (exp/coin/streak/năng lượng)
> trước đây **chỉ trả về sau khi làm 1 hành động nào đó** (submit bài, mở rương...), nên lúc mới
> mở lại app / vừa đăng nhập xong thì FE không có gì để hiển thị ngoài giá trị mặc định hardcode.

### `GET /api/v1/users/me`

Không cần tham số. **Gọi API này đúng 1 lần ngay sau khi có `accessToken`** (login xong, hoặc mỗi
lần app khởi động lại mà token cũ vẫn còn hạn) để hydrate toàn bộ store — không cần đợi user làm
gì cả.

**Response 200 (user vừa đăng ký, chưa làm gì cả):**
```json
{
  "id": 23,
  "email": "user@example.com",
  "displayName": "Stats Test",
  "username": "statstest",
  "role": "LEARNER",
  "level": 1,
  "exp": 0,
  "currentLeague": "BRONZE",
  "coins": 0,
  "currentEnergy": 25,
  "maxEnergy": 25,
  "currentStreak": 0,
  "longestStreak": 0,
  "streakFreezeCount": 0
}
```

Field nào cũng luôn có giá trị thật lấy từ DB (không phải suy đoán) — kể cả khi user chưa từng
tương tác gì. `currentEnergy` đã được tự động hồi trước khi trả về (giống `GET /energy`), nên FE
chỉ cần gọi đúng API này là đủ, không bắt buộc phải gọi thêm `/energy` hay `/streak` nếu chỉ cần
hiển thị số liệu (2 API kia vẫn cần dùng riêng khi cần thêm `lastRecoveryDate`/`lastStreakDate`
để tính cooldown).

`currentLeague` là 1 trong 5 giá trị: `BRONZE`, `SILVER`, `GOLD`, `PLATINUM`, `DIAMOND`.

---

## 3. Format lỗi chung

Mọi lỗi (400/401/403/404/409/500) đều trả về cùng 1 shape JSON:

```json
{
  "timestamp": "2026-08-06T14:10:52.980830300Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Khong du nang luong. Can 10, hien co 8",
  "errors": { "totalQuestions": "must not be null" }
}
```

- `message`: chuỗi mô tả lỗi, **có thể hiển thị trực tiếp cho user** (đã viết bằng tiếng Việt
  không dấu, dễ đọc).
- `errors`: chỉ xuất hiện khi lỗi validate field (thiếu/sai kiểu dữ liệu request body) — map
  `tên field -> lý do lỗi`.

---

## 4. API mới: Daily Quest

Mỗi ngày user được gán ngẫu nhiên 3 nhiệm vụ. Không cần gọi API nào để "khởi tạo" — lần gọi đầu
tiên trong ngày sẽ tự tạo.

### `GET /api/v1/users/me/quests`

**Response 200:**
```json
[
  {
    "questId": 12,
    "title": "Hoan thanh 2 bai hoc",
    "questType": "COMPLETE_LESSONS",
    "currentProgress": 1,
    "targetValue": 2,
    "completed": false
  },
  {
    "questId": 13,
    "title": "Tra loi dung 10 cau",
    "questType": "CORRECT_ANSWERS",
    "currentProgress": 10,
    "targetValue": 10,
    "completed": true
  },
  {
    "questId": 14,
    "title": "Hoan thanh 1 bai khong sai cau nao",
    "questType": "PERFECT_LESSON",
    "currentProgress": 0,
    "targetValue": 1,
    "completed": false
  }
]
```

`questType` là 1 trong 3 giá trị: `COMPLETE_LESSONS`, `CORRECT_ANSWERS`, `PERFECT_LESSON` — FE
dùng để chọn icon minh hoạ nếu cần, không cần xử lý logic gì thêm (BE tự cộng tiến độ).

**Tiến độ được cập nhật tự động** mỗi khi gọi `POST /lessons/{id}/submit` thành công — FE nên gọi
lại `GET /quests` sau mỗi lần submit bài để cập nhật UI (hoặc dùng số liệu đã biết trước để tự
suy đoán, nhưng gọi lại API là chắc chắn nhất).

---

## 5. API mới: Rương thưởng (Chest)

Điều kiện mở rương: **hoàn thành đủ 3 Daily Quest ở trên** + **chưa mở rương hôm nay**.

### `GET /api/v1/users/me/chest`

**Response 200:**
```json
{
  "available": true,
  "alreadyOpenedToday": false,
  "questsCompleted": 3,
  "questsRequired": 3
}
```

Dùng để hiển thị UI rương (khoá/mở, badge "3/3").

### `POST /api/v1/users/me/chest/open`

Không cần request body.

**Response 200 (thành công):**
```json
{
  "coinsRewarded": 82,
  "currentCoins": 154
}
```
`coinsRewarded` ngẫu nhiên trong khoảng **30–100**, roll ở server (FE không đoán trước được, nên
chỉ hiển thị animation mở rương SAU khi có response, không roll số ở client).

**Response 400 (chưa đủ điều kiện):**
```json
{ "status": 400, "message": "Ban da mo ruong hom nay roi, quay lai vao ngay mai." }
```
hoặc
```json
{ "status": 400, "message": "Ban can hoan thanh du 3 nhiem vu hang ngay truoc khi mo ruong." }
```
→ FE nên gọi `GET /chest` trước để biết `available` mà **ẩn/disable nút mở**, tránh để user bấm
rồi mới nhận lỗi.

---

## 6. API mới: Mua Streak Freeze

Streak Freeze bảo vệ chuỗi ngày học liên tiếp khi user nghỉ 1 ngày.

### `POST /api/v1/users/me/streak/freeze/buy`

Không cần request body. Giá cố định: **200 coin**.

**Response 200:**
```json
{
  "currentStreak": 12,
  "longestStreak": 30,
  "lastStreakDate": "2026-08-06",
  "streakFreezeCount": 2
}
```
(cùng shape với `GET /api/v1/users/me/streak` đã có sẵn — `streakFreezeCount` tăng thêm 1 sau khi
mua thành công.)

**Response 400 (thiếu coin):**
```json
{ "status": 400, "message": "Khong du coins. Can 200, hien co 90" }
```

---

## 7. API đổi: Năng lượng (Energy)

> ⚠️ **Đổi số liệu lần 2** — bản trước (`maxEnergy=5`, cost=5) bị lỗi thiết kế (không đủ chỗ để có
> mức thưởng trung gian). Bảng dưới đây là số liệu **mới nhất, hiện hành**.

### 7.1 Giá trị mặc định — **quan trọng, ảnh hưởng UI hiển thị**

| Thông số | Giá trị |
|---|---|
| Năng lượng tối đa (`maxEnergy`) mặc định user mới | **25** |
| Chi phí bắt đầu 1 bài học mặc định (`POST /lessons/{id}/start`) | **10** |
| Hồi thụ động | **+5 mỗi giờ** (đầy từ 0 sau 5 tiếng) |
| Giá hồi đầy bằng coin (`/energy/refill`) | **400 coin** (không đổi) |
| Thưởng xem quảng cáo (`/energy/ads`) | **+5** (không đổi) |

### 7.2 Hoàn năng lượng khi nộp bài tốt — **cơ chế mới, ảnh hưởng trực tiếp `currentEnergy`**

Khi gọi `POST /lessons/{id}/submit`, ngoài EXP/coin, BE còn **hoàn lại một phần năng lượng** đã
trừ lúc `/start`, tuỳ kết quả làm bài (dựa trên `totalMistakes` FE gửi lên):

| Kết quả | Điều kiện | Hoàn lại | Chi phí thực tế (net) |
|---|---|---|---|
| Hoàn hảo | `totalMistakes == 0` | **+5** | 10 − 5 = **5** |
| Khá | `totalMistakes` 1–2 | **+2** | 10 − 2 = **8** |
| Còn lại | `totalMistakes` ≥ 3 | +0 | 10 |

Giá trị hoàn lại này đã được cộng sẵn vào field `currentEnergy` trong response của `/submit` (xem
mục 8) — **FE không cần tự tính, chỉ cần đọc `currentEnergy` để cập nhật thanh năng lượng**.

Lưu ý quan trọng: **làm lại bài đã hoàn thành (replay) không được hoàn năng lượng**, vì replay vốn
đã miễn phí lúc `/start` (tránh user farm năng lượng vô hạn bằng cách replay 1 bài dễ liên tục).

### 7.3 `POST /api/v1/users/me/energy/ads`

Xem quảng cáo (rewarded ad) để hồi năng lượng. Cooldown **30 phút/lần**.

Không cần request body.

**Response 200:**
```json
{ "currentEnergy": 25, "maxEnergy": 25, "lastRecoveryDate": "2026-08-08T19:04:41" }
```
Mỗi lần xem quảng cáo cộng **+5 năng lượng** (cap ở `maxEnergy=25`).

**Response 400 (đã đầy năng lượng):**
```json
{ "status": 400, "message": "Nang luong da day, khong can xem quang cao" }
```

**Response 400 (chưa hết cooldown):**
```json
{ "status": 400, "message": "Vui long cho 27 phut de xem quang cao lan nua" }
```
→ FE nên tự đếm ngược 30 phút từ lần xem gần nhất để **disable nút trước khi gọi API** (tránh
gọi API chỉ để nhận lỗi); số phút còn lại trong message chỉ mang tính hiển thị dự phòng.

### 7.4 Các endpoint không đổi shape, chỉ đổi số liệu

`GET /api/v1/users/me/energy`, `POST /api/v1/users/me/energy/practice`,
`POST /api/v1/users/me/energy/refill` — request/response giữ nguyên, chỉ nội dung số liệu thay
đổi theo bảng 7.1 ở trên.

---

## 8. API đổi: Nộp bài học (Submit Lesson)

### `POST /api/v1/lessons/{id}/submit`

Request giữ nguyên. **Response có thêm field `coinsEarned`:**

```json
{
  "status": "COMPLETED",
  "expEarned": 15,
  "coinsEarned": 12,
  "starsEarned": 0,
  "isTopicCompleted": false,
  "message": "Tuyet voi, ban da hoan thanh bai hoc!",
  "currentEnergy": 20
}
```

`coinsEarned` = coin nhận được từ lần nộp bài này (đã cộng vào ví, không cần gọi thêm API khác để
biết). FE nên hiển thị song song với `expEarned` trên màn hình kết quả ("+15 EXP  +12 coin").

`currentEnergy` ở đây **đã bao gồm phần hoàn lại** nếu làm bài tốt (xem mục 7.2) — ví dụ trên là
user vừa trừ 10 lúc `/start` (từ 25 xuống 15) rồi làm hoàn hảo nên được hoàn +5, ra 20. FE chỉ cần
lấy đúng số này để cập nhật thanh năng lượng, không cần tự cộng trừ.

Quy tắc số coin (tham khảo, không bắt buộc FE tự tính lại):

| Loại bài | Coin cơ bản | Bonus nếu không sai câu nào |
|---|---|---|
| NORMAL | 8 | +4 |
| TIMED_REVIEW | 6 | +3 |
| JUMP_TEST (pass) | 20 | — |

Làm lại bài đã hoàn thành (replay): coin bị nhân theo tỉ lệ replay (mặc định 30%).

Ngoài ra, khi `currentStreak` **vừa đạt đúng** mốc 7 / 30 / 100 ngày, user được cộng thêm
50 / 200 / 1000 coin trong cùng lần submit đó (cũng nằm gộp trong `coinsEarned`).

---

## 9. Bảng tổng hợp lỗi nghiệp vụ cần FE xử lý riêng

| HTTP | `message` (dùng để nhận diện, không parse cứng) | Khi nào xảy ra | FE nên làm gì |
|---|---|---|---|
| 403 | "Bai hoc nay chua duoc mo khoa..." | Start bài đang LOCKED | Không nên xảy ra nếu FE tôn trọng `status` từ roadmap |
| 400 | "Khong du nang luong. Can X, hien co Y" | Start bài thiếu năng lượng | Hiện dialog nạp năng lượng (xem QC / mua bằng coin) |
| 400 | "Khong du coins. Can X, hien co Y" | Refill năng lượng / mua Streak Freeze thiếu coin | Hiện dialog "chưa đủ coin" |
| 400 | "Ban da mo ruong hom nay roi..." | Mở rương lần 2 trong ngày | Ẩn nút mở, hiện đếm ngược tới nửa đêm |
| 400 | "Ban can hoan thanh du 3 nhiem vu..." | Mở rương khi chưa đủ quest | Dẫn user tới màn Daily Quest |
| 400 | "Nang luong da day, khong can xem quang cao" | Xem QC khi đã đầy năng lượng | Ẩn/disable nút xem QC khi `currentEnergy >= maxEnergy` |
| 400 | "Vui long cho N phut..." | Xem QC chưa hết cooldown 30 phút | Disable nút + đếm ngược |

---

*Cập nhật lần cuối: thêm `GET /users/me` (trạng thái tổng hợp) + thiết kế lại kinh tế năng lượng
(max 25, cost 10, hoàn theo kết quả bài học) trên nhánh `feature/coin-reward-chest`. Nếu thấy số
liệu ở đây khác với response API thực tế, ưu tiên tin vào response API / Swagger UI — tài liệu có
thể trễ hơn code.*
