# FE API Guide — Shop & Rank/Leaderboard

> Tài liệu này liệt kê **API mới thêm** và **API đã đổi hành vi/response shape** khi merge 2
> nhánh `feature/shop` và `kiet1` vào `feature/coin-reward-chest`. Đây là 2 tính năng merge từ
> nhánh khác (không phải code viết mới trong các đợt trước), nên đọc kỹ phần "API đổi" — có
> **breaking change** với những gì `FE_API_GUIDE_COIN_QUEST_CHEST_ENERGY.md` đã mô tả trước đó.
> Xem đầy đủ + thử trực tiếp tại Swagger UI: `http://localhost:8090/swagger-ui/index.html`
> (tag **Rank**, **Shop**, **User**).

## Mục lục
1. [Xác thực chung](#1-xác-thực-chung)
2. [⚠️ Breaking change: `GET /users/me` — `currentLeague` bị thay bằng `rankId`/`rankName`](#2--breaking-change-get-usersme--currentleague-bị-thay-bằng-rankidrankname)
3. [API đổi: Nộp bài học — thêm `isPromoted`/`newRankName` + ảnh hưởng powerup Shop](#3-api-đổi-nộp-bài-học--thêm-ispromotednewrankname--ảnh-hưởng-powerup-shop)
4. [API mới: Rank & Leaderboard](#4-api-mới-rank--leaderboard)
5. [API mới: Shop](#5-api-mới-shop)
6. [API mới: Avatar](#6-api-mới-avatar)
7. [Bảng tổng hợp lỗi cần FE xử lý](#7-bảng-tổng-hợp-lỗi-cần-fe-xử-lý)

---

## 1. Xác thực chung

Tất cả API dưới đây yêu cầu header:
```
Authorization: Bearer <accessToken>
```

---

## 2. ⚠️ Breaking change: `GET /users/me` — `currentLeague` bị thay bằng `rankId`/`rankName`

Hệ **League** (enum cứng `BRONZE`/`SILVER`/`GOLD`/`PLATINUM`/`DIAMOND`) đã bị **xoá hoàn toàn**,
thay bằng hệ **Rank** (bảng dữ liệu, có Leaderboard thật đi kèm — xem mục 4).

**Trước đây** (`FE_API_GUIDE_COIN_QUEST_CHEST_ENERGY.md` mục 2):
```json
{ "...": "...", "currentLeague": "BRONZE", "...": "..." }
```

**Bây giờ:**
```json
{
  "id": 23,
  "email": "user@example.com",
  "displayName": "Stats Test",
  "username": "statstest",
  "role": "LEARNER",
  "level": 1,
  "exp": 0,
  "rankId": 1,
  "rankName": "BRONZE",
  "coins": 0,
  "currentEnergy": 25,
  "maxEnergy": 25,
  "currentStreak": 0,
  "longestStreak": 0,
  "streakFreezeCount": 0
}
```

→ **FE cần đổi mọi chỗ đang đọc `currentLeague` sang đọc `rankName`** (giá trị chuỗi vẫn cùng bộ
tên: `BRONZE`, `SILVER`, `GOLD`, `PLATINUM`, `DIAMOND` — chỉ đổi tên field). `rankId` dùng để gọi
`GET /leaderboard?rankId=` (mục 4).

---

## 3. API đổi: Nộp bài học — thêm `isPromoted`/`newRankName` + ảnh hưởng powerup Shop

### `POST /api/v1/lessons/{id}/submit`

Request không đổi. Response có thêm 2 field:

```json
{
  "status": "COMPLETED",
  "expEarned": 30,
  "coinsEarned": 24,
  "starsEarned": 0,
  "isTopicCompleted": false,
  "message": "Tuyet voi, ban da hoan thanh bai hoc!",
  "isPromoted": true,
  "newRankName": "SILVER",
  "currentEnergy": 20
}
```

- `isPromoted`: `true` nếu **vừa** đạt đủ EXP để lên hạng sau lần nộp bài này (do EXP vừa cộng
  trong lượt submit hiện tại). → FE nên hiện animation "Thăng hạng!" khi thấy `true`.
- `newRankName`: tên hạng hiện tại của user sau khi tính lại (luôn có giá trị nếu hệ thống có ít
  nhất 1 rank, không chỉ có khi `isPromoted = true`) — dùng để đồng bộ lại UI hiển thị hạng mà
  không cần gọi lại `GET /users/me`.

**Lưu ý về `expEarned`/`coinsEarned` — có thể tự nhân đôi:** nếu user đang có powerup
**Double XP** hoặc **Double Coin** còn hiệu lực (mua từ Shop, xem mục 5), số EXP/Coin trong
response này **đã được nhân 2 sẵn** — FE chỉ cần hiển thị đúng số nhận được, không cần biết/tính
lại việc có powerup hay không.

---

## 4. API mới: Rank & Leaderboard

> Base path là `/api/v1/ranks` và `/api/v1/leaderboard` — **không** nằm dưới `/users/me` như phần
> lớn API khác, chú ý khi gọi.

### `GET /api/v1/ranks`

Không cần tham số. Danh sách toàn bộ hạng, sắp theo thứ tự tăng dần.

**Response 200:**
```json
[
  { "rankId": 1, "name": "BRONZE", "minExpRequired": 0, "orderIndex": 1 },
  { "rankId": 2, "name": "SILVER", "minExpRequired": 1000, "orderIndex": 2 },
  { "rankId": 3, "name": "GOLD", "minExpRequired": 3000, "orderIndex": 3 },
  { "rankId": 4, "name": "PLATINUM", "minExpRequired": 6000, "orderIndex": 4 },
  { "rankId": 5, "name": "DIAMOND", "minExpRequired": 10000, "orderIndex": 5 }
]
```
Dùng để FE tự vẽ danh sách hạng (vd màn hình "Các cấp bậc"), không đổi thường xuyên nên có thể
cache phía client.

### `GET /api/v1/leaderboard`

Query param `rankId` (**optional**) — không truyền thì mặc định lấy đúng hạng hiện tại của user
đang gọi.

**Response 200:**
```json
{
  "currentRankInfo": { "rankId": 2, "name": "SILVER", "minExpRequired": 1000, "orderIndex": 2 },
  "topUsers": [
    { "userId": 5, "displayName": "An", "username": "an123", "level": 8, "exp": 2400, "rankId": 2, "rankName": "SILVER", "position": null },
    { "userId": 9, "displayName": "Binh", "username": "binh99", "level": 6, "exp": 1800, "rankId": 2, "rankName": "SILVER", "position": null }
  ],
  "currentUserStanding": {
    "userId": 23,
    "exp": 1500,
    "position": 5,
    "message": null
  }
}
```

- `topUsers`: top **15** user có EXP cao nhất **trong đúng `currentRankInfo`** (không phải toàn
  server). ⚠️ **`topUsers[].position` hiện luôn là `null`** (chưa implement số thứ tự cho từng
  dòng trong danh sách) — nếu cần hiển thị "#1, #2, #3..." thì FE tự đánh số theo **thứ tự phần
  tử trong mảng** (mảng đã sắp EXP giảm dần), không dựa vào field `position` này.
- `currentUserStanding.position`: vị trí xếp hạng của **user đang gọi API**, chỉ có giá trị khi
  họ đang ở đúng `rankId` được truy vấn (tức đang xem đúng hạng của chính mình).
- `currentUserStanding.message`: chỉ có giá trị (khác `null`) khi user đang xem 1 hạng **khác**
  hạng hiện tại của họ — cho biết cần thêm bao nhiêu EXP để vào hạng đó, vd:
  ```json
  { "userId": 23, "exp": 1500, "position": null, "message": "Ban can them 500 diem nua de gia nhap hang nay" }
  ```
  → FE dùng field này để hiện thông báo khi user bấm xem 1 hạng cao hơn hạng mình đang ở.

---

## 5. API mới: Shop

> Base path: `/api/v1/users/me/shop`. Đơn vị tiền tệ hiện tại **chỉ dùng coin** (`priceGems` có
> trong response nhưng chưa có cơ chế "gems" nào để tiêu, luôn đọc `priceCoins`).

### 5.1 `GET /api/v1/users/me/shop` — Danh sách vật phẩm

**Response 200** (nhóm sẵn theo `itemType`, key là `CONSUMABLE`/`POWERUP`/`COSMETIC`):
```json
{
  "CONSUMABLE": [
    { "id": 1, "name": "Streak Freeze", "description": "Bao ve chuoi ngay hoc khi ban nghi 1 ngay", "itemType": "CONSUMABLE", "effectType": "STREAK_FREEZE", "effectValue": 1, "priceCoins": 200, "priceGems": 0, "iconUrl": "/icons/streak_freeze.png", "sortOrder": 1, "limitedTime": false, "availableFrom": null, "availableUntil": null }
  ],
  "POWERUP": [
    { "id": 3, "name": "Double XP Boost (30 phut)", "itemType": "POWERUP", "effectType": "DOUBLE_XP", "effectValue": 30, "priceCoins": 150, "priceGems": 0, "...": "..." }
  ],
  "COSMETIC": [
    { "id": 6, "name": "Khung avatar: Sakura", "itemType": "COSMETIC", "effectType": "AVATAR_FRAME", "effectValue": 0, "priceCoins": 500, "priceGems": 0, "...": "..." }
  ]
}
```
`effectType` đầy đủ: `STREAK_FREEZE`, `ENERGY_REFILL` (CONSUMABLE) · `DOUBLE_XP`, `DOUBLE_COIN`,
`TIMER_BOOST` (POWERUP) · `AVATAR_FRAME`, `BADGE`, `THEME` (COSMETIC). `effectValue` ý nghĩa tuỳ
loại: số lượng (STREAK_FREEZE), số năng lượng hồi (ENERGY_REFILL), số phút hiệu lực (POWERUP) —
không có ý nghĩa với COSMETIC (luôn `0`).

### 5.2 `POST /api/v1/users/me/shop/buy/{itemId}` — Mua vật phẩm

Không cần request body.

**Response 200:**
```json
{ "inventoryId": 42, "itemName": "Streak Freeze", "effectType": "STREAK_FREEZE", "coinsSpent": 200, "currentCoins": 350, "message": "Mua thanh cong: Streak Freeze" }
```
Mua trùng 1 item nhiều lần → **cộng dồn `quantity`** trong túi đồ (không tạo dòng inventory mới).

**Lỗi 400 (không đủ coin):** `{ "message": "Khong du coins. Can 200, hien co 90" }`
**Lỗi 400 (item ngừng bán/hết hạn):** `{ "message": "Mat hang khong ton tai hoac da ngung ban." }` hoặc `{ "message": "Mat hang nay hien khong kha dung." }`

### 5.3 `GET /api/v1/users/me/shop/inventory` — Túi đồ

**Response 200:**
```json
[
  { "inventoryId": 42, "itemId": 1, "name": "Streak Freeze", "itemType": "CONSUMABLE", "effectType": "STREAK_FREEZE", "quantity": 2, "equipped": false, "active": true, "expiresAt": null, "acquiredAt": "2026-08-17T10:00:00" },
  { "inventoryId": 50, "itemId": 6, "name": "Khung avatar: Sakura", "itemType": "COSMETIC", "effectType": "AVATAR_FRAME", "quantity": 1, "equipped": true, "active": true, "expiresAt": null, "acquiredAt": "2026-08-16T09:00:00" }
]
```
- `expiresAt`: chỉ có giá trị với item **đã kích hoạt** (powerup đang chạy) — dùng để FE hiện
  đếm ngược "Double XP còn 12 phút".
- `equipped`: chỉ có ý nghĩa với `COSMETIC`, luôn `false` với CONSUMABLE/POWERUP.

### 5.4 `POST /api/v1/users/me/shop/inventory/{inventoryId}/consume` — Dùng/kích hoạt vật phẩm

Dùng cho `CONSUMABLE` (Streak Freeze, Energy Refill) và `POWERUP` (Double XP/Coin, Timer Boost).
Không cần request body. **Trừ 1 khỏi `quantity`** trong túi đồ (hết `quantity` thì item tự biến
mất khỏi túi đồ).

**Response 200:**
```json
{ "itemName": "Double XP Boost (30 phut)", "effectType": "DOUBLE_XP", "effectDescription": "Da kich hoat Double XP Boost (30 phut) trong 30 phut.", "currentCoins": 150, "currentEnergy": 25, "streakFreezeCount": 0, "message": "Su dung thanh cong: Double XP Boost (30 phut)" }
```
`currentCoins`/`currentEnergy`/`streakFreezeCount` chỉ field nào **liên quan effect vừa dùng**
mới thay đổi thật, các field còn lại vẫn trả về giá trị hiện tại của user (không phải `null`).

**Lỗi 400 (cố consume vật phẩm cosmetic):** `{ "message": "Vat pham trang tri khong the su dung, hay trang bi (equip) thay the." }`
**Lỗi 404 (không thuộc về user / không tồn tại / hết số lượng):** `InventoryItemNotFoundException`

### 5.5 `POST /api/v1/users/me/shop/inventory/{inventoryId}/equip` — Trang bị/bỏ trang bị cosmetic

Chỉ dùng cho `COSMETIC`. **Toggle** — gọi lần 1 để trang bị, gọi lại chính API này để bỏ trang bị.
Trang bị 1 item mới cùng loại (vd 2 khung avatar) sẽ **tự bỏ trang bị item cũ cùng `effectType`**
(không thể trang bị 2 khung avatar cùng lúc).

**Response 200:** cùng shape với 1 phần tử trong mục 5.3, field `equipped` phản ánh trạng thái
MỚI sau khi toggle.

**Lỗi 400 (không phải cosmetic):** `{ "message": "Chi co the trang bi vat pham trang tri (cosmetic)." }`

---

## 6. API mới: Avatar

### `GET /api/v1/users/me/avatar`
**Response 200:** `{ "avatarUrl": "https://.../avatar123.png" }` (hoặc `avatarUrl: null` nếu chưa set).

### `PUT /api/v1/users/me/avatar`
**Request:** `{ "avatarUrl": "https://.../avatar123.png" }` (bắt buộc, không rỗng, tối đa 255 ký tự — dùng URL trả về từ API upload file có sẵn, không phải upload trực tiếp ở đây).
**Response:** `204 No Content`.

---

## 7. Bảng tổng hợp lỗi cần FE xử lý

| HTTP | `message` | Khi nào | FE nên làm gì |
|---|---|---|---|
| 400 | "Khong du coins. Can X, hien co Y" | Mua item thiếu coin | Hiện dialog "chưa đủ coin", gợi ý làm bài/mở rương |
| 400 | "Mat hang khong ton tai hoac da ngung ban." / "...hien khong kha dung." | Mua item đã bị admin tắt/hết hạn limited-time | Refetch `GET /shop`, item chắc đã biến mất khỏi danh sách |
| 400 | "Vat pham trang tri khong the su dung..." | Gọi `/consume` cho item COSMETIC | Đổi sang gọi `/equip` |
| 400 | "Chi co the trang bi vat pham trang tri..." | Gọi `/equip` cho item CONSUMABLE/POWERUP | Đổi sang gọi `/consume` |
| 404 | InventoryItemNotFoundException (vật phẩm không thuộc về bạn / hết số lượng) | `/consume`, `/equip` với `inventoryId` sai/của người khác | Refetch `GET /inventory`, không hardcode `inventoryId` |

---

*Cập nhật lần đầu: tài liệu hoá API của 2 nhánh `feature/shop` và `kiet1` sau khi merge vào
`feature/coin-reward-chest`. Nếu thấy số liệu ở đây khác với response API thực tế, ưu tiên tin
vào response API / Swagger UI — tài liệu có thể trễ hơn code.*
