# Test data cho BE_NihongoApp

Bộ dữ liệu này bao phủ **3 hệ thống câu hỏi độc lập** trong codebase (không dùng chung bảng/enum):

| Hệ thống | Bảng | Loại | Cách nạp |
|---|---|---|---|
| Câu hỏi trong bài học | `lesson_questions` + `lesson_question_options` | SELECT_IMAGE, TRANSLATE_TO_JP, TRANSLATE_TO_VN, LISTEN_AND_ARRANGE, LISTEN_AND_SELECT, SPEAKING | API hoặc SQL |
| Câu hỏi đề thi JLPT | `questions` (+ `exams`, `exam_sections`) | không có type riêng, MCQ phẳng | **Chỉ SQL** (không có API tạo) |
| Bảng chữ cái | `characters` | HIRAGANA, KATAKANA | API hoặc SQL |

## 2 file dữ liệu

- `lesson_questions_and_exam_seed.sql` — chạy thẳng trên MySQL, tự tạo topic/lesson/exam làm container, có đủ cả 3 hệ thống trên.
- `api_payloads.json` — payload JSON khớp với các endpoint `POST /api/v1/admin/...`, dùng khi muốn nạp qua API thay vì đụng DB trực tiếp (phần exam JLPT không có API nên vẫn phải lấy từ file .sql).

## Cách dùng file .sql

```bash
mysql -u <user> -p <database_name> < lesson_questions_and_exam_seed.sql
```

Hoặc mở bằng bất kỳ MySQL client nào (DBeaver, MySQL Workbench, TablePlus...) rồi chạy toàn bộ script. Script chỉ INSERT record mới, không sửa/xoá gì cả nên chạy nhiều lần sẽ tạo dữ liệu trùng (không lỗi, nhưng sẽ nhân bản — chỉ chạy 1 lần cho mỗi DB).

## Cách dùng file .json (qua API)

1. Đăng nhập bằng tài khoản admin có sẵn trong DB (`email: admin@nihongo.app`, xem `V12__seed_first_admin.sql` — password đang lưu plaintext `admin123` theo comment trong migration, hãy xác nhận lại nếu đã đổi) để lấy JWT.
2. Gọi lần lượt `step_1_create_topic` → `step_2_create_lesson` → 6 request trong `step_3_create_questions` → `step_4_create_alphabet_bulk`, mỗi bước thay `{{...}}` bằng id trả về ở bước trước.
3. Import file JSON này vào Postman/Thunder Client làm tham khảo nhanh, hoặc copy từng `body` ra để gọi bằng curl:

```bash
curl -X POST http://localhost:8080/api/v1/admin/topics \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Data - N5 Cơ bản","description":"...", "orderIndex":99}'
```

## Lưu ý quan trọng khi tự thêm câu hỏi mới (mọi loại, kể cả SPEAKING)

Service (`AdminContentServiceImpl.validateOptions`) luôn bắt buộc:
- `options` không được rỗng.
- Có ít nhất 1 option `isCorrect = true`.
- Mỗi option phải có ít nhất 1 trong 3 trường `optionText` / `imageUrl` / `audioUrl` khác rỗng.

Thiếu 1 trong 3 điều trên → API trả `400 Bad Request`.

## Cách đưa bộ này cho bạn của bạn

Chọn 1 trong các cách sau, tùy bạn ấy code local hay chỉ test qua API:

1. **Nhanh nhất — gửi trực tiếp 2 file**: gửi `lesson_questions_and_exam_seed.sql` và `api_payloads.json` qua Zalo/Messenger/email/Drive. Đây là cách đơn giản nhất nếu bạn ấy chỉ cần chạy thử.
2. **Nếu bạn ấy có quyền vào cùng repo Git**: commit 2 file này vào một thư mục kiểu `docs/test-data/` hoặc `scripts/seed/` trên nhánh hiện tại (`fix/shop_quantity`) hoặc một nhánh riêng, rồi push — bạn ấy `git pull` là có luôn, không cần gửi file rời. (Mình chưa commit gì cả — nếu bạn muốn theo hướng này, nói mình biết để mình thêm vào repo và commit.)
3. **Nếu bạn ấy chỉ cần test qua API mà không đụng DB**: chỉ cần gửi `api_payloads.json` kèm đường dẫn base URL (`http://localhost:8080` hoặc URL deploy) và tài khoản admin để đăng nhập lấy token.
