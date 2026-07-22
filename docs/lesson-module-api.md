# API: Module Lesson / Vocabulary / Exercise

Tài liệu này mô tả các API vừa được triển khai cho module bài học - từ vựng - bài tập, dành cho đội FE tích hợp.

## 1. Xác thực

Tất cả endpoint (trừ `/api/v1/auth/**`) yêu cầu header:

```
Authorization: Bearer <accessToken>
```

`accessToken` lấy từ API login (`POST /api/v1/auth/login`, thuộc module Auth do người khác phụ trách).

Có 2 nhóm quyền:
- **Learner** (role mặc định `LEARNER`): dùng các API ở mục 3.
- **Admin** (role `ADMIN`): dùng thêm các API quản trị nội dung ở mục 4. Nếu gọi API admin bằng token không phải ADMIN → `403 Forbidden`.

## 2. Quy tắc nghiệp vụ FE cần nắm

- **Trạng thái bài học** (`status`) chỉ có 3 giá trị: `LOCKED`, `IN_PROGRESS`, `COMPLETED`.
- **Mở khóa tuần tự theo `orderIndex`** trong cùng `jlptLevel`: bài có `orderIndex` nhỏ nhất luôn mở sẵn; các bài sau chỉ mở (`IN_PROGRESS`) khi bài ngay trước nó đã `COMPLETED`.
- Gọi `GET /lessons/{id}` khi bài đang `LOCKED` → trả về `403 Forbidden`. FE nên ẩn/disable các bài `LOCKED` trên UI thay vì để user bấm vào rồi nhận lỗi.
- **`correctAnswer` không bao giờ xuất hiện** trong response `GET /lessons/{id}` (xem `exercises[]`) — để tránh lộ đáp án. Đáp án đúng chỉ được trả về **sau khi** gọi API nộp bài (`POST /exercises/{id}/submit`), trong field `correctAnswer` của response, bất kể trả lời đúng hay sai.
- Chấm điểm so khớp chuỗi **không phân biệt hoa/thường** và tự trim khoảng trắng thừa hai đầu.
- **Backend không tự động đánh dấu hoàn thành bài học.** FE phải tự theo dõi việc user đã làm xong hết bài tập trong lesson, rồi chủ động gọi `POST /lessons/{id}/complete` để mở khóa bài kế tiếp.
- `exerciseType` có 4 giá trị: `MULTIPLE_CHOICE`, `TRANSLATION`, `LISTENING`, `MATCHING`. `options` luôn là mảng string đơn giản (không phải object), FE tự render phù hợp theo `exerciseType`.
- `jlptLevel` có 5 giá trị: `N5`, `N4`, `N3`, `N2`, `N1`.

## 3. API cho Learner

### 3.1. Danh sách bài học theo cấp độ

```
GET /api/v1/lessons?level=N5
```

Response `200`:
```json
[
  { "id": 1, "jlptLevel": "N5", "title": "Bai 1: Chao hoi", "orderIndex": 1, "status": "IN_PROGRESS" },
  { "id": 2, "jlptLevel": "N5", "title": "Bai 2: Gia dinh", "orderIndex": 2, "status": "LOCKED" }
]
```

### 3.2. Chi tiết 1 bài học (từ vựng + bài tập)

```
GET /api/v1/lessons/{id}
```

Response `200`:
```json
{
  "id": 1,
  "jlptLevel": "N5",
  "title": "Bai 1: Chao hoi",
  "orderIndex": 1,
  "status": "IN_PROGRESS",
  "vocabularies": [
    {
      "id": 1,
      "kanji": "こんにちは",
      "furigana": "こんにちは",
      "romaji": "konnichiwa",
      "meaningVn": "Xin chao",
      "exampleSentence": "こんにちは、元気ですか。",
      "exampleMeaning": "Xin chao, ban co khoe khong?",
      "audioUrl": null
    }
  ],
  "exercises": [
    {
      "id": 1,
      "exerciseType": "MULTIPLE_CHOICE",
      "questionText": "Xin chao trong tieng Nhat la gi?",
      "options": ["Konnichiwa", "Arigatou", "Sayonara", "Ohayou"]
    }
  ]
}
```
Lỗi: `403` nếu bài đang khóa, `404` nếu `id` không tồn tại.

### 3.3. Nộp đáp án 1 bài tập

```
POST /api/v1/exercises/{id}/submit
Content-Type: application/json

{ "answer": "konnichiwa" }
```

Response `200`:
```json
{ "correct": true, "correctAnswer": "Konnichiwa" }
```
Lỗi: `404` nếu `id` bài tập không tồn tại.

### 3.4. Đánh dấu hoàn thành bài học

```
POST /api/v1/lessons/{id}/complete
```
Response `204 No Content` (không có body). Sau khi gọi, bài kế tiếp (`orderIndex + 1`) sẽ chuyển từ `LOCKED` sang `IN_PROGRESS` ở lần gọi `GET /lessons` tiếp theo.
Lỗi: `403` nếu bài đang khóa (không thể complete bài chưa được mở), `404` nếu không tồn tại.

## 4. API cho Admin (quản lý nội dung, yêu cầu role `ADMIN`)

Base path: `/api/v1/admin`

| Method | Path | Body | Mô tả |
|---|---|---|---|
| POST | `/admin/lessons` | `{ "jlptLevel", "title", "orderIndex" }` | Tạo bài học mới |
| PUT | `/admin/lessons/{id}` | như trên | Cập nhật bài học |
| DELETE | `/admin/lessons/{id}` | - | Xóa mềm (soft-delete) |
| POST | `/admin/vocabularies` | `{ "lessonId", "kanji", "furigana", "romaji", "meaningVn", "exampleSentence", "exampleMeaning", "audioUrl" }` | Tạo từ vựng |
| PUT | `/admin/vocabularies/{id}` | như trên | Cập nhật từ vựng |
| DELETE | `/admin/vocabularies/{id}` | - | Xóa mềm |
| POST | `/admin/exercises` | `{ "lessonId", "exerciseType", "questionText", "correctAnswer", "options": ["..."] }` | Tạo bài tập |
| PUT | `/admin/exercises/{id}` | như trên | Cập nhật bài tập |
| DELETE | `/admin/exercises/{id}` | - | Xóa cứng (bảng `exercises` không có cột soft-delete) |

Response của API admin cho `exercises` (`ExerciseAdminResponse`) **có** field `correctAnswer` — khác với response phía Learner ở mục 3.2 (`ExercisePlayResponse`) không có field này.

## 5. Mã lỗi chung

| HTTP status | Khi nào xảy ra |
|---|---|
| 400 | Body request thiếu field bắt buộc / sai định dạng (lỗi validate) |
| 403 | Không có token / token không đủ quyền (learner gọi API admin) / bài học đang khóa |
| 404 | `lessonId`/`vocabularyId`/`exerciseId` không tồn tại hoặc đã bị xóa |

## 6. Những phần CHƯA tích hợp (để FE không nhầm là bug)

- Hoàn thành bài học **chưa** tự cộng exp/coin cho user (module gamification làm riêng sau).
- **Chưa có** cơ chế ôn tập từ vựng theo lịch (spaced repetition).
- **Chưa có** API để nâng quyền user lên `ADMIN` — việc này hiện phải làm trực tiếp trên DB (`UPDATE users SET role='ADMIN' WHERE ...`).
