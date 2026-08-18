# BE_NihongoApp

## Biến môi trường

| Biến | Bắt buộc? | Default (local dev) | Production |
|---|---|---|---|
| `DB_USERNAME` | Không | `root` | Set theo user MySQL thực tế của môi trường production |
| `DB_PASSWORD` | Không (local) / **Có** (production) | Có giá trị mặc định cứng trong `application.yml` — xem cảnh báo bên dưới | **BẮT BUỘC** set theo mật khẩu MySQL thực tế, không dùng giá trị mặc định trong repo |
| `JWT_SECRET` | Không (local) / **Có** (production) | `change-me-in-dev-change-me-in-dev-change-me-in-dev` (giữ nguyên giá trị cũ để token hiện có không bị vô hiệu) | **BẮT BUỘC** set bằng chuỗi ngẫu nhiên ≥ 32 byte, ví dụ sinh bằng `openssl rand -base64 32` |
| `UPLOAD_DIR` | Không | `./uploads` (đường dẫn tương đối, tính từ thư mục chạy app) | Nên set đường dẫn tuyệt đối tới thư mục lưu file, ví dụ `/var/www/nihongo-app/uploads` |

**Local dev không cần set gì.** Production **BẮT BUỘC** set `JWT_SECRET` bằng chuỗi ngẫu nhiên ≥ 32 byte.

> ⚠️ `DB_PASSWORD` hiện có giá trị mặc định là một mật khẩu thật hardcode trong
> `src/main/resources/application.yml` (không phải placeholder). Đây là rủi ro bảo mật cần xử lý
> riêng — xem phần báo cáo cuối của tác vụ dọn cấu hình để biết chi tiết.
