# FE API Guide — Bản tin cộng đồng (Social Feed)

> Tài liệu này liệt kê **API mới thêm** và **1 API đã đổi shape response** cho module Social Feed
> (follow bạn bè, feed tổng hợp, đăng trạng thái, like, comment). Toàn bộ API còn lại không đổi —
> xem đầy đủ + thử trực tiếp tại **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
> (tag **User**, **Social Feed - Feed**, **Social Feed - Posts**). Đổi `8080` thành cổng thật nếu
> team bạn chạy app ở cổng khác.

## Mục lục
1. [Xác thực chung](#1-xác-thực-chung)
2. [Khái niệm & quy ước](#2-khái-niệm--quy-ước)
3. [Phân trang kiểu cursor (dùng chung 5 API)](#3-phân-trang-kiểu-cursor-dùng-chung-5-api)
4. [Follow](#4-follow)
5. [Hồ sơ công khai](#5-hồ-sơ-công-khai)
6. [API đổi: Tìm kiếm user](#6-api-đổi-tìm-kiếm-user)
7. [Feed](#7-feed)
8. [Đăng bài / Like / Comment](#8-đăng-bài--like--comment)
9. [Bài đăng thành tích hệ thống (SYSTEM_ACHIEVEMENT)](#9-bài-đăng-thành-tích-hệ-thống-system_achievement)
10. [Bảng tổng hợp lỗi / edge case cần FE xử lý](#10-bảng-tổng-hợp-lỗi--edge-case-cần-fe-xử-lý)

---

## 1. Xác thực chung

Tất cả API dưới đây yêu cầu header:

```
Authorization: Bearer <accessToken>
```

## 2. Khái niệm & quy ước

- **Follow 1 chiều**: A follow B không có nghĩa B follow lại A. Không có khái niệm "kết bạn 2 chiều".
- **Idempotent**: follow/unfollow/like/unlike gọi lặp lại (double-tap, mất mạng phải retry...)
  **không bao giờ trả lỗi** — gọi 2 lần cho cùng kết quả với gọi 1 lần.
- **Soft delete**: xoá post/comment chỉ ẩn khỏi API đọc (`deleted_at` được set), không xoá dữ liệu
  thật trong DB.
- **Không lộ dữ liệu riêng tư**: hồ sơ công khai (`/users/{id}/profile`) không có email/coins/energy
  — chỉ có thông tin phù hợp để hiển thị công khai.

## 3. Phân trang kiểu cursor (dùng chung 5 API)

Feed, followers, following, comment, search đều dùng chung 1 kiểu phân trang — không phải
`page`/`totalPages` kiểu cũ:

```json
{
  "items": [ /* ... */ ],
  "nextCursor": "MjAyNi0wOC0yNFQxMDozMDowMH41"
}
```

- **Trang đầu**: gọi không kèm `cursor`.
- **Trang sau**: lấy nguyên chuỗi `nextCursor` của response trước, gắn vào query param `cursor`
  của lần gọi sau. **Không tự parse/decode chuỗi này** — coi như 1 token mờ (opaque).
- **Hết dữ liệu**: `nextCursor` = `null` → dừng gọi thêm.
- `size` mặc định 20 cho tất cả (trừ ghi chú riêng).

---

## 4. Follow

### `PUT /api/v1/users/{id}/follow` — Follow

- Idempotent: đã follow rồi gọi lại → 204, không đổi gì.
- Tự follow chính mình (`id` = chính mình) → **400 Bad Request**.

**Response:** `204 No Content`

### `DELETE /api/v1/users/{id}/follow` — Unfollow

- Idempotent: chưa follow gọi vẫn → 204, không lỗi.

**Response:** `204 No Content`

> Endpoint cũ `POST /api/v1/users/{id}/follow` (toggle follow/unfollow theo trạng thái hiện tại,
> trả `{"true"/"false"}` báo trạng thái mới) **vẫn giữ nguyên, không đổi** — dùng cho màn hình cũ
> nếu đang dùng. Khuyến nghị màn hình mới dùng cặp PUT/DELETE ở trên vì rõ ràng, dễ đồng bộ state
> ở FE hơn (không cần đọc response để biết đã follow hay chưa).

### `GET /api/v1/users/{id}/followers?cursor=&size=20` — Danh sách người theo dõi

### `GET /api/v1/users/{id}/following?cursor=&size=20` — Danh sách đang theo dõi

**Response 200 (2 API trên có cùng shape):**
```json
{
  "items": [
    {
      "id": 12,
      "displayName": "Yamada Taro",
      "avatarUrl": "https://cdn.example.com/avatars/12.png",
      "isFollowing": true
    }
  ],
  "nextCursor": null
}
```
- `isFollowing`: **người xem hiện tại** (không phải chủ danh sách) có đang follow người này không —
  dùng để hiện nút "Follow back" ngay trong danh sách followers.

---

## 5. Hồ sơ công khai

### `GET /api/v1/users/{id}/profile`

**Response 200:**
```json
{
  "id": 12,
  "displayName": "Yamada Taro",
  "avatarUrl": "https://cdn.example.com/avatars/12.png",
  "rankName": "SILVER",
  "currentStreak": 5,
  "followerCount": 34,
  "followingCount": 21,
  "isFollowing": false
}
```
- Khác `GET /api/v1/users/profile/{username}` (endpoint cũ, tra theo username, field khác) —
  endpoint mới này tra theo **id**, dành riêng cho luồng Social Feed (bấm vào avatar 1 post/comment
  → xem hồ sơ). Không có email/coins/energy.

---

## 6. API đổi: Tìm kiếm user

### `GET /api/v1/users/search?q=&cursor=&size=20`

**⚠️ Thay đổi so với trước:**
- Response đổi từ **mảng trần** `[...]` sang **object có `nextCursor`**: `{"items": [...], "nextCursor": ...}`.
- Thêm param `q` (từ khoá tìm kiếm). Param `keyword` cũ **vẫn nhận được** (alias tương thích ngược)
  — nhưng dùng `q` cho code mới.
- Thêm phân trang cursor (trước đây trả về tất cả kết quả 1 lần).

**Response 200:**
```json
{
  "items": [
    { "id": 12, "displayName": "Yamada Taro", "avatarUrl": "...", "level": 8, "isFollowing": false }
  ],
  "nextCursor": null
}
```

---

## 7. Feed

### `GET /api/v1/feed?cursor=&size=20`

Bản tin tổng hợp: bài của **chính mình** + bài của **những người đang follow**, mới nhất trước.

**Response 200:**
```json
{
  "items": [
    {
      "id": 501,
      "author": { "id": 12, "displayName": "Yamada Taro", "avatarUrl": "..." },
      "postType": "USER_STATUS",
      "content": "Hôm nay học xong bài Kanji N4 đầu tiên!",
      "createdAt": "2026-08-24T09:12:00",
      "likeCount": 3,
      "commentCount": 1,
      "likedByMe": false
    },
    {
      "id": 500,
      "author": { "id": 12, "displayName": "Yamada Taro", "avatarUrl": "..." },
      "postType": "SYSTEM_ACHIEVEMENT",
      "content": "Đã đạt thành tích \"Người mới bắt đầu\"! Hoàn thành bài học đầu tiên.",
      "createdAt": "2026-08-23T20:00:00",
      "likeCount": 5,
      "commentCount": 0,
      "likedByMe": true
    }
  ],
  "nextCursor": "MjAyNi0wOC0yM1QyMDowMDowMH41MDA"
}
```
- `postType`: `"USER_STATUS"` (user tự đăng) hoặc `"SYSTEM_ACHIEVEMENT"` (tự động khi unlock thành
  tích — xem [mục 9](#9-bài-đăng-thành-tích-hệ-thống-system_achievement)). FE nên hiện icon/khung
  khác nhau cho 2 loại này (vd huy hiệu vàng cho SYSTEM_ACHIEVEMENT).
- Feed rỗng (chưa follow ai, tự mình cũng chưa đăng gì) → `"items": []`, `"nextCursor": null`.

---

## 8. Đăng bài / Like / Comment

### `POST /api/v1/posts` — Đăng trạng thái

```json
{ "content": "Hôm nay học xong bài Kanji N4 đầu tiên!" }
```
- `content`: bắt buộc, tối đa 500 ký tự.
- `postType` luôn là `USER_STATUS` — không có param nào cho FE tự chọn postType (SYSTEM_ACHIEVEMENT
  chỉ do BE tự tạo).

**Response:** `201 Created`, không có body.

### `DELETE /api/v1/posts/{id}` — Xoá bài (soft delete)

- Chỉ **chủ bài đăng** hoặc **ADMIN** được xoá. Xoá bài người khác → **403 Forbidden**.

**Response:** `204 No Content`

### `POST /api/v1/posts/{id}/like` / `DELETE /api/v1/posts/{id}/like`

- Idempotent — like 2 lần liên tiếp (double-tap) không lỗi, không tăng đôi `likeCount`.

**Response:** `204 No Content`

### `GET /api/v1/posts/{id}/comments?cursor=&size=20`

**Response 200:**
```json
{
  "items": [
    {
      "id": 33,
      "author": { "id": 7, "displayName": "Suzuki Hana", "avatarUrl": "..." },
      "content": "Chúc mừng nhé!",
      "createdAt": "2026-08-24T09:20:00"
    }
  ],
  "nextCursor": null
}
```

### `POST /api/v1/posts/{id}/comments` — Thêm comment

```json
{ "content": "Chúc mừng nhé!" }
```
- `content`: bắt buộc, tối đa 300 ký tự.

**Response:** `201 Created`, không có body.

### `DELETE /api/v1/comments/{id}` — Xoá comment (soft delete)

- Chỉ **chủ comment** hoặc **ADMIN**. Xoá comment người khác → **403 Forbidden**.
- Lưu ý path gốc là `/api/v1/comments/{id}`, **không phải** `/api/v1/posts/{postId}/comments/{id}`.

**Response:** `204 No Content`

---

## 9. Bài đăng thành tích hệ thống (SYSTEM_ACHIEVEMENT)

Khi user unlock 1 achievement (hệ thống Achievement có sẵn, không đổi gì ở đó), BE **tự động**
tạo 1 post `postType = SYSTEM_ACHIEVEMENT` xuất hiện ngay trong feed của người đó và người đang
follow họ — FE **không cần gọi thêm API nào** để kích hoạt việc này, chỉ cần hiển thị đúng khi thấy
`postType = "SYSTEM_ACHIEVEMENT"` trong response feed (xem mẫu ở mục 7).

- Chỉ tạo đúng **1 lần** tại thời điểm unlock — không tạo lại nếu tiến độ (progress) của achievement
  đã unlock được cập nhật thêm sau đó.
- `content` do BE tự soạn sẵn (tên + mô tả achievement) — FE hiển thị nguyên văn, không cần tự ghép chuỗi.

---

## 10. Bảng tổng hợp lỗi / edge case cần FE xử lý

| Tình huống | HTTP status | Ghi chú FE |
|---|---|---|
| Follow chính mình | 400 | Nên ẩn nút Follow trên hồ sơ của chính mình thay vì đợi lỗi |
| Follow/Unfollow gọi lặp lại | 204 (không lỗi) | Không cần debounce phía FE để tránh lỗi — nhưng vẫn nên debounce để giảm request |
| Like/Unlike gọi lặp lại (double-tap) | 204 (không lỗi) | `likeCount` không tăng đôi |
| Xoá post/comment không phải của mình | 403 | Ẩn nút xoá nếu `author.id !== currentUserId` để tránh gọi API thừa |
| Xoá post/comment không tồn tại | 404 | Có thể do đã bị xoá ở tab khác — nên refetch danh sách |
| `content` rỗng khi đăng post/comment | 400 | Validate phía FE trước khi gọi API để tránh round-trip thừa |
| `content` vượt quá độ dài (500/300 ký tự) | 400 | Validate độ dài phía FE (đếm ký tự khi gõ) |
| Gửi `cursor` sai định dạng/hỏng | Coi như trang đầu (không lỗi) | Không cần xử lý riêng — BE tự bỏ qua cursor không hợp lệ |
