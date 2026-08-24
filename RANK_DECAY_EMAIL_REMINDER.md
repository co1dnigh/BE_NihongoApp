# Rank Decay và Email Nhắc Nhở

## 1. Tổng quan

Backend đã bổ sung cơ chế giảm EXP và tự giáng rank khi user không học bài trong một khoảng thời gian cấu hình được.

Cơ chế hiện tại dùng trực tiếp `users.exp`:

```text
Không học đủ 7 ngày
-> Trừ 100 EXP
-> Tính lại rank theo EXP mới
-> Leaderboard phản ánh EXP và rank mới
```

Đây là cơ chế **rolling theo từng user**, không phải tất cả user đều reset vào cùng một ngày trong tuần.

`exp` bị giảm theo yêu cầu sản phẩm. Tiến trình bài học trong các bảng progress không bị xóa.

## 2. Các thay đổi backend

### Database

Migration [V38__add_rank_decay_tracking.sql](src/main/resources/db/migration/V38__add_rank_decay_tracking.sql) thêm vào bảng `users`:

| Cột | Ý nghĩa |
|---|---|
| `last_learning_at` | Thời điểm gần nhất user hoàn thành hoạt động có nhận EXP |
| `last_rank_decay_at` | Mốc decay gần nhất đã được xử lý |
| `last_rank_reminder_at` | Thời điểm reminder gần nhất đã được xếp lịch gửi |

Migration được Flyway tự động chạy khi backend khởi động.

### Entity và repository

`User` đã map ba cột thời gian mới. `UserRepository` có query lấy các learner chưa bị xóa và đã từng học để job xử lý.

### Rank maintenance

Logic nằm trong [RankMaintenanceService.java](src/main/java/com/example/nihongo_app/service/RankMaintenanceService.java).

Job decay:

```text
Mặc định chạy mỗi ngày lúc 00:00:00
Timezone: Asia/Ho_Chi_Minh
```

Job không chỉ trừ một lần. Ví dụ user nghỉ 21 ngày thì hệ thống xử lý ba chu kỳ 7 ngày và trừ ba lần.

EXP mới được giới hạn tối thiểu ở `0`. Sau khi trừ, backend gọi rank repository để lấy rank cao nhất thỏa mãn:

```text
rank.min_exp_required <= user.exp
```

Nếu rank mới khác rank hiện tại, `users.rank_id` được cập nhật. Leaderboard hiện có đã query theo `exp`, nên không cần API leaderboard mới.

## 3. Hoạt động nào cập nhật thời gian học

Các luồng nhận EXP sau đã cập nhật `last_learning_at`:

- Hoàn thành lesson
- Review lesson có nhận EXP
- Jump test có nhận EXP
- Alphabet practice

Khi user học lại, backend:

- Cập nhật `last_learning_at`
- Xóa `last_rank_decay_at`
- Xóa `last_rank_reminder_at`

Như vậy user bắt đầu lại một chu kỳ 7 ngày mới.

User cũ chưa có `last_learning_at` sẽ chưa bị decay và chưa nhận reminder cho đến khi hoàn thành một hoạt động học mới.

## 4. Email reminder

Backend không cung cấp API để FE trực tiếp gửi email. Email được gửi tự động từ scheduler.

Job reminder:

```text
Mặc định chạy mỗi ngày lúc 18:00:00
Timezone: Asia/Ho_Chi_Minh
```

Job tìm user đã không học gần đủ 7 ngày. Khi còn khoảng 1 ngày trước kỳ decay, backend:

1. Đánh dấu `last_rank_reminder_at` để chống gửi lặp.
2. Gửi email bất đồng bộ bằng `@Async`.
3. Nếu gửi thất bại, ghi log lỗi thay vì làm hỏng request hoặc scheduler chính.

Email được tạo trong [EmailServiceImpl.java](src/main/java/com/example/nihongo_app/service/impl/EmailServiceImpl.java).

## 5. Cấu hình email thật

Dependency đã được thêm vào [pom.xml](pom.xml):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

Cấu hình trong [application.yml](src/main/resources/application.yml) dùng biến môi trường:

```yaml
spring:
  mail:
    host: ${MAIL_HOST:}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}

app:
  mail:
    enabled: ${MAIL_ENABLED:false}
  rank:
    reminder-enabled: ${RANK_REMINDER_ENABLED:false}
    mail-from: ${RANK_MAIL_FROM:}
```

Ví dụ dùng Gmail SMTP:

```powershell
$env:MAIL_ENABLED="true"
$env:RANK_REMINDER_ENABLED="true"
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="sender@gmail.com"
$env:MAIL_PASSWORD="gmail-app-password"
$env:RANK_MAIL_FROM="sender@gmail.com"
```

`MAIL_PASSWORD` phải là Gmail App Password, không phải mật khẩu Gmail thông thường. Không đưa các biến SMTP này vào React Native.

Nếu chưa cấu hình SMTP, backend vẫn khởi động nhưng email sẽ bị bỏ qua và ghi cảnh báo log.

## 6. Cấu hình decay

Giá trị mặc định:

```yaml
app:
  rank:
    inactivity-days: 7
    decay-exp: 100
    decay-cron: 0 0 0 * * *
    reminder-cron: 0 0 18 * * *
```

Có thể override bằng biến môi trường:

```powershell
$env:RANK_INACTIVITY_DAYS="7"
$env:RANK_DECAY_EXP="100"
$env:RANK_DECAY_CRON="0 0 0 * * *"
$env:RANK_REMINDER_CRON="0 0 18 * * *"
```

Cron dùng định dạng 6 trường của Spring, trong đó trường đầu tiên là giây.

## 7. FE cần tích hợp gì

FE không cần gọi API reset rank hoặc API gửi email.

FE chỉ cần:

1. Gọi API hoàn thành lesson/alphabet practice như bình thường.
2. Đọc `exp` và thông tin rank từ response hoặc API user/leaderboard.
3. Khi user đăng nhập lại, tải lại profile/leaderboard để thấy EXP và rank mới.
4. Hiển thị thông báo email hoặc rank mới nếu sản phẩm cần, nhưng không tự tính decay ở FE.

Luồng chính:

```text
React Native hoàn thành bài
-> Backend cộng EXP và ghi last_learning_at
-> Scheduler backend kiểm tra hằng ngày
-> Đủ thời gian nghỉ: trừ EXP và cập nhật rank
-> Reminder email gửi trước kỳ trừ điểm
-> FE đọc dữ liệu mới từ backend
```

## 8. Kiểm thử

### Test tự động

Test logic decay nhiều chu kỳ và reminder:

```powershell
.\mvnw.cmd -Dtest=RankMaintenanceServiceTest test
```

Chạy toàn bộ test:

```powershell
.\mvnw.cmd test
```

### Test thủ công decay

Sau khi có user trong database, có thể giả lập user đã nghỉ 8 ngày:

```powershell
docker compose exec db mysql -u nihongo_user -p"1234" nihongo_db -e "UPDATE users SET exp = 500, last_learning_at = NOW() - INTERVAL 8 DAY, last_rank_decay_at = NULL WHERE email = 'user@example.com';"
```

Để chạy decay mỗi phút trong môi trường local:

```powershell
$env:RANK_DECAY_CRON="0 * * * * *"
.\mvnw.cmd spring-boot:run
```

Kiểm tra kết quả:

```powershell
docker compose exec db mysql -u nihongo_user -p"1234" nihongo_db -e "SELECT email, exp, rank_id, last_learning_at, last_rank_decay_at FROM users WHERE email = 'user@example.com';"
```

### Test thủ công email

Giả lập user còn một ngày trước decay:

```powershell
docker compose exec db mysql -u nihongo_user -p"1234" nihongo_db -e "UPDATE users SET last_learning_at = NOW() - INTERVAL 6 DAY, last_rank_reminder_at = NULL WHERE email = 'user@example.com';"
```

Chạy reminder mỗi phút trong local:

```powershell
$env:RANK_REMINDER_CRON="0 * * * * *"
.\mvnw.cmd spring-boot:run
```

Email nhận có thể là bất kỳ email hợp lệ nào, ví dụ `hotisun17102005@gmail.com`. Tuy nhiên phải có một tài khoản SMTP hợp lệ để làm tài khoản gửi.

## 9. Lưu ý production

- Dùng `Asia/Ho_Chi_Minh` trực tiếp trong `@Scheduled` và trong các timestamp nghiệp vụ.
- Không gửi SMTP password lên Git hoặc đưa xuống FE.
- Với nhiều instance backend, cần thêm distributed lock như ShedLock hoặc một cơ chế lock DB để tránh hai instance cùng decay/gửi reminder.
- Với lượng user lớn, nên thay việc tải toàn bộ user bằng pagination/batch query.
- Nên có bảng outbox/email log nếu cần retry email thất bại một cách đáng tin cậy.
- Không đổi tên hoặc xóa migration đã chạy trên database; mọi thay đổi schema mới phải tạo migration version mới.
