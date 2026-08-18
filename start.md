# Hướng dẫn chạy BE_NihongoApp

## 1. Yêu cầu hệ thống
- **Java 17+** (đang dùng JDK 25.0.3)
- **MySQL 8.4** qua Docker Compose (port 3307, xem `docker-compose.yml`)
- **Maven** (có `mvnw` wrapper)

## 2. Cấu hình Database (application.yml)

```bash
# Từ thư mục BE_NihongoApp, bật MySQL + phpMyAdmin qua Docker Compose trước
docker compose up -d
```

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/nihongo_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
    username: nihongo_user
    password: 1234
```

> Đây là giá trị mặc định khớp `docker-compose.yml` (không cần set biến môi trường gì thêm cho dev local).
> Database `nihongo_db` sẽ được Flyway tự tạo schema qua toàn bộ migration (hiện tại V1→V31) khi app khởi động lần đầu.

## 3. Chạy ứng dụng

```bash
# Từ thư mục BE_NihongoApp
./mvnw spring-boot:run
```

App chạy tại: **http://localhost:8080**

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI spec: http://localhost:8080/v3/api-docs

## 4. Test API nhanh

```bash
# 1. Đăng ký user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Test1234!","displayName":"Demo"}'

# 2. Đăng nhập lấy JWT token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Test1234!"}' | jq -r .accessToken)

# 3. Xem trạng thái gamification tổng hợp (mới đăng ký đã có dữ liệu thật)
curl http://localhost:8080/api/v1/users/me -H "Authorization: Bearer $TOKEN"

# 4. Xem Daily Quest hôm nay
curl http://localhost:8080/api/v1/users/me/quests -H "Authorization: Bearer $TOKEN"

# 5. Xem trạng thái Rương thưởng
curl http://localhost:8080/api/v1/users/me/chest -H "Authorization: Bearer $TOKEN"

# 6. Start bài học (trừ 10 energy)
curl -X POST http://localhost:8080/api/v1/lessons/1/start -H "Authorization: Bearer $TOKEN"

# 7. Submit bài học (hoàn hảo: 20/20, 0 sai)
curl -X POST http://localhost:8080/api/v1/lessons/1/submit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"totalQuestions":20,"totalCorrect":20,"totalMistakes":0}'
```

## 5. Chạy test

### Unit tests (không cần DB)
```bash
./mvnw test -Dtest="ChestServiceTest,DailyQuestServiceTest,EnergyServiceTest,LessonUnlockPolicyTest,RoadmapServiceImplTest,StreakServiceTest,UserServiceImplTest,LessonAttemptServiceImplTest,ShopServiceTest"
# Kết quả: 78 test pass
```

### Tất cả test (cần MySQL đang chạy)
```bash
./mvnw test
```

## 6. Các endpoint chính (cần JWT Bearer token)

| Method | Path | Mô tả |
|--------|------|-------|
| GET | `/api/v1/users/me` | Trạng thái tổng hợp (level, exp, coins, energy, streak, league) |
| GET | `/api/v1/users/me/quests` | 3 Daily Quest hôm nay (tự random nếu chưa có) |
| GET | `/api/v1/users/me/chest` | Trạng thái rương thưởng |
| POST | `/api/v1/users/me/chest/open` | Mở rương (nhận 30-100 coin ngẫu nhiên) |
| POST | `/api/v1/users/me/streak/freeze/buy` | Mua Streak Freeze (200 coin) |
| POST | `/api/v1/users/me/energy/refill` | Hồi đầy năng lượng (400 coin) |
| POST | `/api/v1/lessons/{id}/start` | Bắt đầu bài học (trừ energy) |
| POST | `/api/v1/lessons/{id}/submit` | Nộp bài (cộng coin, exp, cập nhật quest, streak) |

## 7. Lưu ý quan trọng

- **Flyway baseline-on-migrate=true**: Lần đầu chạy trên DB trống sẽ apply tất cả migration V1-V24
- **Energy system**: max_energy=25, entry cost=10, hồi +5/giờ, hoàn +5 nếu perfect, +2 nếu good (1-2 sai)
- **Coin faucets**: hoàn thành bài (8-20), mốc streak 7/30/100 (50/200/1000), rương hàng ngày (30-100)
- **Coin sinks**: refill energy (400), streak freeze (200)
- **Replay protection**: replay bài đã hoàn thành không tốn energy, coin/exp chỉ nhận 30% (configurable)

## 8. Troubleshooting

### Lỗi "Access denied for user 'nihongo_user'"
- Kiểm tra container DB đang chạy: `docker compose ps`
- Kiểm tra password trong `application.yml` khớp với `docker-compose.yml` (mặc định `nihongo_user` / `1234`)
- Test: `docker compose exec db mysql -u nihongo_user -p"1234" -e "SELECT 1;"`

### Lỗi Flyway migration (Unknown column)
- Drop database và chạy lại (dùng root của container, mặc định `rootchangeme`):
  `docker compose exec db mysql -u root -p"rootchangeme" -e "DROP DATABASE nihongo_db; CREATE DATABASE nihongo_db;"`
- Flyway sẽ tự chạy lại từ V1

### Port 8080 bị chiếm
```bash
SERVER_PORT=8090 ./mvnw spring-boot:run
```

---
*Cập nhật: 2026-08-17 - Nhánh feature/coin-reward-chest*