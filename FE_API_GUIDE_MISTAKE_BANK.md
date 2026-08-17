# FE API Guide — Mistake Bank (Ngân hàng lỗi sai)

> Tài liệu này liệt kê **API mới thêm** và **API đã đổi hành vi** cho tính năng Mistake Bank
> trên nhánh `feature/coin-reward-chest`. Toàn bộ API còn lại không đổi — xem đầy đủ + thử trực
> tiếp tại **Swagger UI**: `http://localhost:8090/swagger-ui/index.html` (tag **Mistake Review**).

## Mục lục
1. [Xác thực chung](#1-xác-thực-chung)
2. [Khái niệm](#2-khái-niệm)
3. [API đổi: Nộp bài học — thêm `answers`](#3-api-đổi-nộp-bài-học--thêm-answers)
4. [API mới: `GET /reviews/mistakes/summary`](#4-api-mới-get-reviewsmistakessummary)
5. [API mới: `POST /reviews/mistakes/start`](#5-api-mới-post-reviewsmistakesstart)
6. [API mới: `POST /reviews/mistakes/submit`](#6-api-mới-post-reviewsmistakessubmit)
7. [Luồng UX đề xuất](#7-luồng-ux-đề-xuất)
8. [Bảng tổng hợp lỗi / edge case cần FE xử lý](#8-bảng-tổng-hợp-lỗi--edge-case-cần-fe-xử-lý)

---

## 1. Xác thực chung

Tất cả API dưới đây yêu cầu header:

```
Authorization: Bearer <accessToken>
```

---

## 2. Khái niệm

- **Mistake**: 1 câu hỏi mà user từng trả lời sai trong bài học thường (NORMAL/TIMED_REVIEW,
  không tính JUMP_TEST, không tính lúc replay). Mỗi câu chỉ có 1 record (không nhân bản), BE
  tự dồn `wrong_count` mỗi lần sai lại.
- **Trạng thái**: `ACTIVE` (còn cần ôn) hoặc `RESOLVED` (đã "xoá nợ").
- **Xoá nợ**: cần trả lời đúng **2 lần**, và 2 lần đúng đó phải **cách nhau tối thiểu 1 ngày**.
  → Trả lời đúng 2 lần liên tiếp trong cùng 1 phiên ôn **không** đủ để xoá nợ ngay — đây là thiết
  kế cố ý để chống học vẹt đáp án. FE nên set kỳ vọng cho user đúng vậy (vd tooltip "Ôn đúng
  liên tục nhiều ngày để xoá hẳn lỗi sai này").
- **Phiên ôn tập**: tách biệt hoàn toàn với bài học thường — không trừ năng lượng lúc bắt đầu,
  không cộng EXP/Coin, chỉ thưởng năng lượng (giới hạn lượt/ngày) lúc nộp bài.

---

## 3. API đổi: Nộp bài học — thêm `answers`

### `POST /api/v1/lessons/{id}/submit`

Request có thêm 1 field **optional**, hoàn toàn tương thích ngược (không gửi field này thì API
hoạt động y như trước):

```json
{
  "totalQuestions": 10,
  "totalCorrect": 8,
  "totalMistakes": 2,
  "answers": [
    { "questionId": 101, "selectedOptionId": 501 },
    { "questionId": 102, "selectedOptionId": 512 }
  ]
}
```

- `answers`: danh sách **toàn bộ câu đã làm** trong lượt này (không chỉ câu sai) — mỗi phần tử
  chỉ cần `questionId` + `selectedOptionId` (option user đã chọn). **BE tự tra DB để xác định
  đúng/sai, FE không cần và không thể tự khai báo `isCorrect`.**
- Nếu FE gửi `answers`, BE sẽ tự động: (a) lưu lại chi tiết từng câu, (b) với câu SAI trong bài
  NORMAL/TIMED_REVIEW (không tính replay) → tự thêm/cập nhật vào Mistake Bank.
- **Không gửi `answers` = tính năng Mistake Bank không hoạt động cho lượt đó** (không lỗi, chỉ
  đơn giản là không có dữ liệu để ghi nhận) — nên gửi đầy đủ nếu muốn user dùng được tính năng ôn
  lỗi sai.
- Response giữ nguyên 100% như trước (không có field mới trong response của endpoint này).

---

## 4. API mới: `GET /reviews/mistakes/summary`

Không cần tham số. Dùng để hiển thị badge/preview trên màn hình chính (vd "5 lỗi cần ôn").

**Response 200:**
```json
{
  "activeCount": 5,
  "reviewableToday": 2
}
```

- `activeCount`: tổng số câu đang `ACTIVE` (cần ôn), không giới hạn số này.
- `reviewableToday`: số phiên ôn **còn được thưởng năng lượng** trong hôm nay (0–2, xem mục 6).
  FE dùng để hiển thị dạng "Ôn ngay +5⚡ (còn 2 lượt)". Về **0** không có nghĩa là không ôn được
  nữa — user vẫn ôn bình thường, chỉ là không nhận thêm năng lượng (xem mục 6).

---

## 5. API mới: `POST /reviews/mistakes/start`

Không cần request body. **Không trừ năng lượng.**

**Response 200 (có lỗi sai để ôn):**
```json
{
  "questions": [
    {
      "questionId": 101,
      "questionType": "TRANSLATE_TO_JP",
      "content": "Con mèo",
      "audioUrl": null,
      "imageUrl": null,
      "metadataJson": null,
      "options": [
        { "optionId": 501, "content": "猫", "imageUrl": null, "audioUrl": null, "metadataJson": null, "order": null },
        { "optionId": 502, "content": "犬", "imageUrl": null, "audioUrl": null, "metadataJson": null, "order": null }
      ]
    }
  ],
  "message": null
}
```

- Lấy tối đa **10 câu** (ưu tiên câu sai nhiều nhất, sai gần nhất), thứ tự câu và thứ tự option
  đã được xáo trộn sẵn — FE hiển thị đúng thứ tự nhận được, không tự sort lại.
- `questionType` là 1 trong: `SELECT_IMAGE`, `TRANSLATE_TO_JP`, `TRANSLATE_TO_VN`,
  `LISTEN_AND_ARRANGE`, `LISTEN_AND_SELECT`, `SPEAKING` — dùng để chọn UI render giống hệt cách
  FE đang xử lý cho bài học thường.
- **Quan trọng: option KHÔNG có field `isCorrect`** (khác với `POST /lessons/{id}/start`) — FE
  không tự chấm được, phải đợi response của `/submit` (mục 6) mới biết đúng/sai.

**Response 200 (không có lỗi sai nào cần ôn — không phải lỗi):**
```json
{
  "questions": [],
  "message": "Ban chua co loi sai nao can on tap, tiep tuc hoc bai moi nhe!"
}
```
→ FE kiểm tra `questions.length === 0` để hiển thị `message` thay vì màn hình làm bài (vd ẩn hẳn
nút "Ôn lỗi sai" nếu `GET /summary` đã cho biết `activeCount = 0` từ trước, để tránh user bấm vào
rồi mới thấy trống).

---

## 6. API mới: `POST /reviews/mistakes/submit`

Request — danh sách câu đã làm trong phiên ôn (cùng shape `answers` như mục 3):

```json
{
  "answers": [
    { "questionId": 101, "selectedOptionId": 501 },
    { "questionId": 103, "selectedOptionId": 520 }
  ]
}
```

**Response 200:**
```json
{
  "results": [
    { "questionId": 101, "correct": true, "correctOptionId": 501, "resolved": false },
    { "questionId": 103, "correct": false, "correctOptionId": 522, "resolved": false }
  ],
  "resolvedCount": 0,
  "energyRewarded": 5,
  "currentEnergy": 20
}
```

- `results[].correct`: BE tự chấm từ DB (đối chiếu `selectedOptionId`), không phải FE tự gửi.
- `results[].correctOptionId`: đáp án đúng thật của câu đó — dùng để hiển thị "Đáp án đúng là..."
  sau khi user trả lời (kể cả khi họ trả lời đúng, để xác nhận).
- `results[].resolved`: `true` nếu **đúng lần này khiến câu đó vừa được xoá nợ** (chuyển
  `RESOLVED`) — dùng để hiện hiệu ứng ăn mừng riêng cho câu đó (vd confetti/tích xanh).
- `resolvedCount`: tổng số câu vừa `resolved = true` trong lần nộp này (tiện để hiện tổng, không
  cần FE tự đếm lại từ `results`).
- `energyRewarded`: năng lượng vừa được cộng — **`0` nếu nộp bài hợp lệ nhưng đã hết lượt thưởng
  trong ngày** (xem quy tắc dưới) — vẫn chấm điểm/cập nhật Mistake Bank bình thường, chỉ là không
  có năng lượng.
- `currentEnergy`: năng lượng hiện tại sau khi cộng thưởng (nếu có) — FE chỉ cần đọc field này để
  cập nhật thanh năng lượng, không cần tự cộng dồn.

### Quy tắc thưởng năng lượng
- Nộp bài **hợp lệ** (≥ 1 câu được chấm) → **+5 năng lượng**, cap ở `maxEnergy`.
- Tối đa **2 phiên được thưởng/ngày** (đếm theo ngày thực, reset lúc sang ngày mới) — phiên thứ 3
  trở đi trong ngày vẫn chấm điểm/cập nhật Mistake Bank bình thường, chỉ không cộng thêm năng
  lượng (`energyRewarded: 0`).
- **Không cộng EXP/Coin** cho phiên ôn tập (tránh farm) — chỉ có năng lượng.

### Answer không hợp lệ
Nếu 1 phần tử trong `answers` có `questionId` không tồn tại trong Mistake Bank của user (vd đã bị
xoá nợ từ phiên khác, hoặc FE gửi nhầm câu không phải của mình), hoặc `selectedOptionId` không
thuộc đúng `questionId` → BE **âm thầm bỏ qua phần tử đó** (không có trong `results`, không lỗi
500/400). Nếu toàn bộ `answers` đều không hợp lệ, `results` sẽ là mảng rỗng và
`energyRewarded = 0`.

---

## 7. Luồng UX đề xuất

```
Màn hình chính
  → GET /summary  (hiển thị badge "N lỗi cần ôn" + nút "Ôn ngay +5⚡ (còn X lượt)")
  → User bấm "Ôn ngay"
  → POST /start
     - questions rỗng -> hiện message, không vào màn làm bài
     - có questions -> vào màn làm bài, render giống bài học thường (KHÔNG có đáp án đúng sẵn)
  → User làm hết các câu
  → POST /submit  (gửi answers)
     - hiện kết quả từng câu (đúng/sai + đáp án đúng)
     - hiện hiệu ứng riêng cho câu có resolved=true
     - cập nhật thanh năng lượng theo currentEnergy
     - nếu energyRewarded = 0 -> không hiện animation +5⚡ (đã hết lượt hôm nay)
  → (tuỳ chọn) GET /summary lại để cập nhật badge cho lần sau
```

---

## 8. Bảng tổng hợp lỗi / edge case cần FE xử lý

| Tình huống | HTTP | Cách BE xử lý | FE nên làm gì |
|---|---|---|---|
| `POST /start` khi không có mistake ACTIVE | 200 | `questions: []` + `message` | Hiện `message`, không coi là lỗi |
| `POST /submit` với answer không hợp lệ/không thuộc user | 200 | Âm thầm bỏ qua phần tử đó | Không cần xử lý riêng — chỉ hiển thị đúng `results` trả về (có thể ít hơn số answer đã gửi) |
| `POST /submit` sau khi hết lượt thưởng năng lượng/ngày | 200 | `energyRewarded: 0`, vẫn chấm điểm bình thường | Không hiện animation cộng năng lượng, vẫn hiện kết quả đúng/sai |
| Trả lời đúng 2 lần trong cùng 1 phiên | 200 | `correctStreak` tăng nhưng `resolved: false` (chưa đủ cách 1 ngày) | Không báo "đã xoá nợ" — có thể hiện "cần ôn lại vào ngày khác để xoá hẳn" |
| Chưa đăng nhập / token hết hạn | 401 | `{ "message": "Missing authenticated principal" }` | Điều hướng về màn login, giống các API khác |

---

*Cập nhật lần đầu: thêm Mistake Bank (ghi answer từng câu, ngân hàng lỗi sai, phiên ôn tập) trên
nhánh `feature/coin-reward-chest`. Nếu thấy số liệu ở đây khác với response API thực tế, ưu tiên
tin vào response API / Swagger UI — tài liệu có thể trễ hơn code.*
