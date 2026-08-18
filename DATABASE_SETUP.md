# Kết nối Database — BE_NihongoApp

## 1. Tổng quan
- Database: **MySQL 8.4**, chạy qua Docker Compose (`docker-compose.yml` ở thư mục gốc).
- Không cần cài MySQL riêng trên máy — chỉ cần Docker.

## 2. Khởi động

```bash
docker compose up -d
```

Lệnh này bật 2 container:
- `db` — MySQL 8.4, expose ra host ở **port 3307**.
- `phpmyadmin` — UI xem dữ liệu, tại http://localhost:8085

Kiểm tra đã chạy: `docker compose ps` — cả 2 container phải ở trạng thái `Up`, riêng `db` phải là `healthy`.

## 3. Thông tin kết nối mặc định

| | Giá trị mặc định | Override qua biến môi trường |
|---|---|---|
| Host | `localhost` | — |
| Port | `3307` | `DB_PORT` |
| Database | `nihongo_db` | `DB_NAME` |
| User (app dùng) | `nihongo_user` | `DB_USERNAME` |
| Password (app dùng) | `1234` | `DB_PASSWORD` |
| User admin container | `root` | — |
| Password admin container | `rootchangeme` | `DB_ROOT_PASSWORD` |

Các giá trị này khớp sẵn giữa `docker-compose.yml` và default trong `application.yml`
(`spring.datasource.*`) — **dev local không cần set biến môi trường nào cả**, chỉ cần
`docker compose up -d` rồi chạy app là kết nối được ngay.

## 4. Vì sao port là 3307, không phải 3306 mặc định của MySQL?

Nếu máy đã có sẵn 1 MySQL khác (cài native, không qua Docker) thì nó thường tự chiếm
port 3306. `docker-compose.yml` chủ động map container ra port khác để tránh đụng độ:

```yaml
ports:
  - "${DB_PORT:-3307}:3306"
```

Nếu `application.yml` lỡ trỏ về 3306 trong khi Docker đang chạy ở 3307, Spring Boot sẽ
kết nối **nhầm vào MySQL native** (nếu máy có) thay vì Docker — sinh ra lỗi
`Access denied` dù Docker vẫn đang chạy hoàn toàn bình thường. Đây chính xác là lỗi đã
gặp và fix trên nhánh này — nếu thấy lại lỗi tương tự, kiểm tra port trước tiên.

## 5. Kiểm tra kết nối thủ công

```bash
# Qua mysql client bên trong container (không cần cài mysql client trên máy host)
docker compose exec db mysql -u nihongo_user -p"1234" -e "SELECT 1;"
```

Hoặc mở phpMyAdmin tại http://localhost:8085 (server `db`, user `root`, password `rootchangeme`).

## 6. Chạy backend

```bash
./mvnw spring-boot:run
```

Log khởi động sẽ hiện `Database: jdbc:mysql://localhost:3307/nihongo_db ...`, và Flyway
tự áp toàn bộ migration (V1 → hiện tại V31) nếu là database trống.

## 7. Troubleshooting

### `Access denied for user 'nihongo_user'@'localhost'`
1. Docker chưa chạy → `docker compose up -d`.
2. Có tiến trình khác đang chiếm port 3307 (hiếm) → đổi `DB_PORT` (biến môi trường hoặc
   file `.env` cạnh `docker-compose.yml`) rồi `docker compose up -d` lại.
3. `application.yml`/biến môi trường đang trỏ sai port/user/password → đối chiếu lại
   bảng ở mục 3.

### Flyway báo lỗi migration (schema không khớp / muốn làm lại từ đầu)

```bash
docker compose exec db mysql -u root -p"rootchangeme" \
  -e "DROP DATABASE nihongo_db; CREATE DATABASE nihongo_db;"
```

Flyway sẽ tự chạy lại từ V1 ở lần khởi động app kế tiếp.

### Muốn xem dữ liệu trực quan
Mở phpMyAdmin: http://localhost:8085 (server `db`, user `root`, password `rootchangeme`).

## 8. Biến môi trường khác (không thuộc DB)

Xem đầy đủ ở `README.md` mục "Biến môi trường" (`JWT_SECRET`, `UPLOAD_DIR`) — local dev
cũng không cần set gì thêm, chỉ production mới bắt buộc.
