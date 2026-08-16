# Shop Module API - Postman Test Documentation (Actual Response)

Tất cả API đều yêu cầu **Authorization: Bearer \<token\>**.

---

## 1. GET /api/v1/users/me/shop
**Xem danh sách vật phẩm trong shop (nhóm theo loại)**

| | |
|---|---|
| **Method** | GET |
| **URL** | `http://localhost:8080/api/v1/users/me/shop` |
| **Headers** | `Authorization: Bearer {{accessToken}}` |
| **Body** | Không có |

### Response 200 (Actual)

```json
{
  "POWERUP": [
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Nhận gấp đôi EXP trong 30 phút",
      "effectType": "DOUBLE_XP",
      "effectValue": 30,
      "iconUrl": "/icons/double_xp.png",
      "id": 3,
      "itemType": "POWERUP",
      "limitedTime": false,
      "name": "Double XP Boost (30 phút)",
      "priceCoins": 150,
      "priceGems": 0,
      "sortOrder": 3
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Nhận gấp đôi Coin trong 30 phút",
      "effectType": "DOUBLE_COIN",
      "effectValue": 30,
      "iconUrl": "/icons/double_coin.png",
      "id": 4,
      "itemType": "POWERUP",
      "limitedTime": false,
      "name": "Double Coin Boost (30 phút)",
      "priceCoins": 200,
      "priceGems": 0,
      "sortOrder": 4
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Tăng thời gian làm bài Timed Review thêm 50% trong 10 phút",
      "effectType": "TIMER_BOOST",
      "effectValue": 10,
      "iconUrl": "/icons/timer_boost.png",
      "id": 5,
      "itemType": "POWERUP",
      "limitedTime": false,
      "name": "Timer Boost (10 phút)",
      "priceCoins": 100,
      "priceGems": 0,
      "sortOrder": 5
    }
  ],
  "CONSUMABLE": [
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Bảo vệ chuỗi ngày học khi bạn nghỉ 1 ngày",
      "effectType": "STREAK_FREEZE",
      "effectValue": 1,
      "iconUrl": "/icons/streak_freeze.png",
      "id": 1,
      "itemType": "CONSUMABLE",
      "limitedTime": false,
      "name": "Streak Freeze",
      "priceCoins": 200,
      "priceGems": 0,
      "sortOrder": 1
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Hồi đầy năng lượng ngay lập tức",
      "effectType": "ENERGY_REFILL",
      "effectValue": 25,
      "iconUrl": "/icons/energy_refill.png",
      "id": 2,
      "itemType": "CONSUMABLE",
      "limitedTime": false,
      "name": "Energy Refill",
      "priceCoins": 400,
      "priceGems": 0,
      "sortOrder": 2
    }
  ],
  "COSMETIC": [
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Khung avatar chủ đề hoa anh đào",
      "effectType": "AVATAR_FRAME",
      "effectValue": 0,
      "iconUrl": "/icons/frame_sakura.png",
      "id": 6,
      "itemType": "COSMETIC",
      "limitedTime": false,
      "name": "Khung avatar: Sakura",
      "priceCoins": 500,
      "priceGems": 0,
      "sortOrder": 10
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Khung avatar núi Phú Sĩ",
      "effectType": "AVATAR_FRAME",
      "effectValue": 0,
      "iconUrl": "/icons/frame_fuji.png",
      "id": 7,
      "itemType": "COSMETIC",
      "limitedTime": false,
      "name": "Khung avatar: Fuji",
      "priceCoins": 500,
      "priceGems": 0,
      "sortOrder": 11
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Khung avatar đèn neon Tokyo",
      "effectType": "AVATAR_FRAME",
      "effectValue": 0,
      "iconUrl": "/icons/frame_neon.png",
      "id": 8,
      "itemType": "COSMETIC",
      "limitedTime": false,
      "name": "Khung avatar: Neon Tokyo",
      "priceCoins": 800,
      "priceGems": 0,
      "sortOrder": 12
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Huy hiệu hiển thị trên hồ sơ",
      "effectType": "BADGE",
      "effectValue": 0,
      "iconUrl": "/icons/badge_samurai.png",
      "id": 9,
      "itemType": "COSMETIC",
      "limitedTime": false,
      "name": "Huy hiệu: Samurai",
      "priceCoins": 300,
      "priceGems": 0,
      "sortOrder": 13
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Huy hiệu hiển thị trên hồ sơ",
      "effectType": "BADGE",
      "effectValue": 0,
      "iconUrl": "/icons/badge_ninja.png",
      "id": 10,
      "itemType": "COSMETIC",
      "limitedTime": false,
      "name": "Huy hiệu: Ninja",
      "priceCoins": 300,
      "priceGems": 0,
      "sortOrder": 14
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Giao diện tối cho ứng dụng",
      "effectType": "THEME",
      "effectValue": 0,
      "iconUrl": "/icons/theme_dark.png",
      "id": 11,
      "itemType": "COSMETIC",
      "limitedTime": false,
      "name": "Chủ đề: Dark Mode",
      "priceCoins": 1000,
      "priceGems": 0,
      "sortOrder": 15
    },
    {
      "availableFrom": null,
      "availableUntil": null,
      "description": "Giao diện hồng hoa anh đào",
      "effectType": "THEME",
      "effectValue": 0,
      "iconUrl": "/icons/theme_sakura.png",
      "id": 12,
      "itemType": "COSMETIC",
      "limitedTime": false,
      "name": "Chủ đề: Sakura Pink",
      "priceCoins": 1000,
      "priceGems": 0,
      "sortOrder": 16
    }
  ]
}
```

---

## 2. POST /api/v1/users/me/shop/buy/{itemId}
**Mua vật phẩm từ shop**

| | |
|---|---|
| **Method** | POST |
| **URL** | `http://localhost:8080/api/v1/users/me/shop/buy/{{itemId}}` |
| **Headers** | `Authorization: Bearer {{accessToken}}` |
| **Body** | Không có |

### Test Cases

| itemId | Item | Price | Note |
|--------|------|-------|------|
| 1 | Streak Freeze | 200 coins | CONSUMABLE |
| 2 | Energy Refill | 400 coins | CONSUMABLE |
| 3 | Double XP Boost | 150 coins | POWERUP |
| 4 | Double Coin Boost | 200 coins | POWERUP |
| 5 | Timer Boost | 100 coins | POWERUP |
| 6 | Avatar Frame: Sakura | 500 coins | COSMETIC |
| 7 | Avatar Frame: Fuji | 500 coins | COSMETIC |
| 8 | Avatar Frame: Neon Tokyo | 800 coins | COSMETIC |
| 9 | Badge: Samurai | 300 coins | COSMETIC |
| 10 | Badge: Ninja | 300 coins | COSMETIC |
| 11 | Theme: Dark Mode | 1000 coins | COSMETIC |
| 12 | Theme: Sakura Pink | 1000 coins | COSMETIC |

### Response 200 (Success)
```json
{
  "itemId": 1,
  "itemName": "Streak Freeze",
  "coinsSpent": 200,
  "currentCoins": 800,
  "effectType": "STREAK_FREEZE",
  "inventoryId": 10
}
```

### Error Responses

**400 - InsufficientCoinsException**
```json
{
  "timestamp": "2026-08-17T...",
  "status": 400,
  "error": "Bad Request",
  "message": "Không đủ coin. Cần: 200, Hiện có: 100"
}
```

**400 - ItemNotAvailableException**
```json
{
  "message": "Vật phẩm không còn bán hoặc đã hết hạn"
}
```

---

## 3. GET /api/v1/users/me/shop/inventory
**Xem túi đồ của user**

| | |
|---|---|
| **Method** | GET |
| **URL** | `http://localhost:8080/api/v1/users/me/shop/inventory` |
| **Headers** | `Authorization: Bearer {{accessToken}}` |
| **Body** | Không có |

### Response 200
```json
[
  {
    "id": 10,
    "itemId": 1,
    "itemName": "Streak Freeze",
    "itemType": "CONSUMABLE",
    "effectType": "STREAK_FREEZE",
    "quantity": 1,
    "equipped": false,
    "expiresAt": null,
    "acquiredFrom": "SHOP_BUY",
    "createdAt": "2026-08-17T10:30:00"
  },
  {
    "id": 11,
    "itemId": 3,
    "itemName": "Double XP Boost (30 phút)",
    "itemType": "POWERUP",
    "effectType": "DOUBLE_XP",
    "quantity": 1,
    "equipped": false,
    "expiresAt": "2026-08-17T11:00:00",
    "acquiredFrom": "SHOP_BUY",
    "createdAt": "2026-08-17T10:30:00"
  },
  {
    "id": 12,
    "itemId": 6,
    "itemName": "Khung avatar: Sakura",
    "itemType": "COSMETIC",
    "effectType": "AVATAR_FRAME",
    "quantity": 1,
    "equipped": true,
    "expiresAt": null,
    "acquiredFrom": "SHOP_BUY",
    "createdAt": "2026-08-17T10:30:00"
  }
]
```

---

## 4. POST /api/v1/users/me/shop/inventory/{inventoryId}/consume
**Sử dụng vật phẩm tiêu hao / kích hoạt powerup**

| | |
|---|---|
| **Method** | POST |
| **URL** | `http://localhost:8080/api/v1/users/me/shop/inventory/{{inventoryId}}/consume` |
| **Headers** | `Authorization: Bearer {{accessToken}}` |
| **Body** | Không có |

### Responses by EffectType

| EffectType | Item | Response |
|------------|------|----------|
| STREAK_FREEZE | Streak Freeze | `{"effectType":"STREAK_FREEZE","effectDescription":"Đã sử dụng Streak Freeze. Tổng freeze: 1","streakFreezeCount":1}` |
| ENERGY_REFILL | Energy Refill | `{"effectType":"ENERGY_REFILL","effectDescription":"Đã hồi đầy năng lượng (25/25)","currentEnergy":25}` |
| DOUBLE_XP | Double XP Boost | `{"effectType":"DOUBLE_XP","effectDescription":"Kích hoạt Double XP: 30 phút"}` |
| DOUBLE_COIN | Double Coin Boost | `{"effectType":"DOUBLE_COIN","effectDescription":"Kích hoạt Double Coin: 30 phút"}` |
| TIMER_BOOST | Timer Boost | `{"effectType":"TIMER_BOOST","effectDescription":"Kích hoạt Timer Boost: 10 phút"}` |

### Error 400 (Cosmetic)
```json
{
  "message": "Vật phẩm trang trí không thể sử dụng (consume)"
}
```

### Error 404 (Not found / Wrong user)
```json
{
  "message": "Không tìm thấy vật phẩm trong túi đồ"
}
```

---

## 5. POST /api/v1/users/me/shop/inventory/{inventoryId}/equip
**Trang bị / Bỏ trang bị vật phẩm trang trí**

| | |
|---|---|
| **Method** | POST |
| **URL** | `http://localhost:8080/api/v1/users/me/shop/inventory/{{inventoryId}}/equip` |
| **Headers** | `Authorization: Bearer {{accessToken}}` |
| **Body** | Không có |

### Chỉ hoạt động với COSMETIC items:
- AVATAR_FRAME (id 6, 7, 8)
- BADGE (id 9, 10)
- THEME (id 11, 12)

### Response 200 - Equip (lần đầu)
```json
{
  "id": 12,
  "itemId": 6,
  "itemName": "Khung avatar: Sakura",
  "itemType": "COSMETIC",
  "effectType": "AVATAR_FRAME",
  "quantity": 1,
  "equipped": true,
  "expiresAt": null,
  "acquiredFrom": "SHOP_BUY",
  "createdAt": "2026-08-17T10:30:00"
}
```

### Response 200 - Unequip (gọi lại lần 2)
```json
{
  "equipped": false,
  ...
}
```

### Error 400 (Not cosmetic)
```json
{
  "message": "Chỉ có thể trang bị vật phẩm trang trí"
}
```

---

## Quick Test Flow in Postman

```
1. Auth → Login (token auto saved to {{accessToken}})

2. GET /shop
   → Copy itemId muốn mua (vd: 1 = Streak Freeze)

3. POST /shop/buy/1
   → Response có inventoryId (vd: 10)

4. GET /shop/inventory
   → Xác nhận item đã vào túi

5. POST /shop/inventory/10/consume
   → Dùng Streak Freeze

6. POST /shop/buy/3
   → Mua Double XP Boost (150 coins)

7. POST /shop/inventory/11/consume
   → Kích hoạt Double XP (set expiresAt +30 phút)

8. POST /shop/buy/6
   → Mua Avatar Frame: Sakura (500 coins)

9. POST /shop/inventory/12/equip
   → Trang bị (equipped: true)

10. POST /shop/inventory/12/equip
    → Bỏ trang bị (equipped: false)
```

---

## Item Reference Table (from actual response)

| ID | Name | Type | Effect | Price | effectValue |
|----|------|------|--------|-------|-------------|
| 1 | Streak Freeze | CONSUMABLE | STREAK_FREEZE | 200 | 1 |
| 2 | Energy Refill | CONSUMABLE | ENERGY_REFILL | 400 | 25 |
| 3 | Double XP Boost | POWERUP | DOUBLE_XP | 150 | 30 (phút) |
| 4 | Double Coin Boost | POWERUP | DOUBLE_COIN | 200 | 30 (phút) |
| 5 | Timer Boost | POWERUP | TIMER_BOOST | 100 | 10 (phút) |
| 6 | Avatar Frame: Sakura | COSMETIC | AVATAR_FRAME | 500 | 0 |
| 7 | Avatar Frame: Fuji | COSMETIC | AVATAR_FRAME | 500 | 0 |
| 8 | Avatar Frame: Neon Tokyo | COSMETIC | AVATAR_FRAME | 800 | 0 |
| 9 | Badge: Samurai | COSMETIC | BADGE | 300 | 0 |
| 10 | Badge: Ninja | COSMETIC | BADGE | 300 | 0 |
| 11 | Theme: Dark Mode | COSMETIC | THEME | 1000 | 0 |
| 12 | Theme: Sakura Pink | COSMETIC | THEME | 1000 | 0 |

---

## Notes

- **priceGems**: Hiện tại tất cả = 0 (chỉ dùng coin)
- **limitedTime**: Tất cả false (không giới hạn thời gian bán)
- **availableFrom/availableUntil**: null (luôn có sẵn)
- **iconUrl**: Đường dẫn icon (FE tự handle)
- **CONSUMABLE**: Dùng 1 lần → biến mất khỏi inventory
- **POWERUP**: Dùng 1 lần → set expiresAt, duration = effectValue (phút)
- **COSMETIC**: Không consume được, chỉ equip/unequip