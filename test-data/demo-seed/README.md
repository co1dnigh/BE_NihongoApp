# Bộ dữ liệu demo — BE_NihongoApp

Bộ dữ liệu học tiếng Nhật hoàn chỉnh cho người Việt **chưa biết một chữ tiếng Nhật nào**,
kèm 30 tài khoản học viên ở đủ mọi trạng thái để demo được tất cả tính năng hiện có.

Toàn bộ ảnh và âm thanh là **file thật, nằm trong `uploads/`** — demo không cần Internet.

---

## 1. Có gì trong bộ này

| Hạng mục | Số lượng |
|---|---|
| Chủ đề (topic) | 14 — toàn bộ là chủ đề tình huống đời thường |
| Bài học (lesson) | 100 — 84 bài thường, 14 bài ôn tập tính giờ, 2 bài thi vượt cấp |
| Câu hỏi | 1.592 |
| Đáp án | 6.146 |
| Câu **có âm thanh** | 596 / 1.592 (37%) — đúng bằng số câu mà nghe là việc phải làm |
| Câu **có hình ảnh** | 375 câu ở phần đề bài + 143 câu có 4 thẻ đáp án bằng ảnh |
| File âm thanh MP3 | 824 (giọng neural `ja-JP-NanamiNeural`, chậm hơn 10% cho người mới) |
| File ảnh PNG | 505 |
| Ký tự bảng chữ cái có âm thanh | 208 / 208 |
| Học viên demo | 28 (+ admin và 2 tài khoản cá nhân sẵn có) |

### Bản đồ lộ trình KHÔNG còn bài dạy bảng chữ cái

Ứng dụng đã có một trang học chữ riêng (đọc từ bảng `characters`, đủ 208 ký tự kèm âm
thanh và tiến độ thông thạo riêng của từng người học). Để 24 bài mặt chữ nằm trên bản đồ
nữa là dạy trùng, và bắt người mới cày hết bảng chữ cái trước khi được nói một câu tiếng
Nhật nào — ngược hẳn với cách Duolingo dẫn người học.

Nên lộ trình bây giờ **chỉ gồm chủ đề tình huống**, và không câu nào trên bản đồ hỏi về
một ký tự đơn lẻ. Hàm `build_kana_topic` trong `build.py` vẫn còn nguyên phòng khi muốn
dựng lại, chỉ là `build()` không gọi tới.

### Lộ trình học (đúng thứ tự mở khoá)

| # | Chủ đề | Bài | Nội dung chính |
|---|---|---|---|
| 1 | Chào hỏi hằng ngày | 8 | Chào theo buổi, cảm ơn/xin lỗi, làm quen, tạm biệt, ra vào nhà, bữa ăn |
| 2 | Giới thiệu bản thân | 7 | 〜は〜です, quốc tịch, nghề nghiệp, câu hỏi か, phủ định, trợ từ の |
| 3 | Số đếm, tuổi và giá tiền | 7 | 1–10, 11–100, hàng vạn, tuổi, hỏi giá, đơn vị đếm |
| 4 | Đồ vật quanh ta | 7 | これ/それ/あれ, đồ học tập, đồ dùng, この/その/あの, sở hữu |
| 5 | Địa điểm và vị trí | 7 | ここ/そこ/あそこ, nơi chốn, あります/います, từ chỉ vị trí, hỏi đường |
| 6 | Thời gian và lịch | 7 | Giờ, phút, buổi, thứ, tháng ngày, 〜から〜まで |
| 7 | Động từ và sinh hoạt hằng ngày | 8 | Thể ます, trợ từ を/へ/で/に, quá khứ, phủ định, kể một ngày |
| 8 | Ăn uống và nhà hàng | 7 | Thức ăn, đồ uống, món Nhật, gọi món, mùi vị, hội thoại nhà hàng |
| 9 | Gia đình và con người | 6 | 2 bộ từ gia đình (khiêm nhường / kính trọng), đếm người |
| 10 | Tính từ và miêu tả | 7 | Tính từ い / な, màu sắc, thời tiết, phủ định, nối 2 tính từ |
| 11 | Mua sắm ở cửa hàng | 7 | Loại cửa hàng, quần áo, hỏi giá, chọn cỡ/màu, giảm giá, hội thoại mua hàng |
| 12 | Đi lại và phương tiện | 7 | Phương tiện, nhà ga, lên/xuống xe, hỏi đường, đi mất bao lâu, tàu trễ |
| 13 | Sở thích và ngày nghỉ | 7 | Sở thích, thể thao, phim/nhạc, 〜がすき, 〜がじょうず, trạng từ tần suất |
| 14 | Sức khoẻ và cơ thể | 8 | Bộ phận cơ thể, 〜がいたい, bệnh viện và thuốc, cảm giác, lời khuyên, đi khám |

Thứ tự này có lý do: chủ đề mua sắm đứng sau số đếm và tính từ (cần đếm tiền, cần
đắt/rẻ), chủ đề đi lại đứng sau địa điểm và thời gian, chủ đề sở thích đứng sau động từ.

---

## 1b. Ba quy tắc về âm thanh và hình ảnh

Đây là phần dễ làm ẩu nhất, và làm ẩu thì người dùng thấy ngay.

**Nút loa chỉ xuất hiện khi âm thanh CHÍNH LÀ đề bài.**

| Dạng câu | Âm thanh | Vì sao |
|---|---|---|
| `LISTEN_AND_SELECT` | có | Nghe rồi chọn — không nghe thì không làm được |
| `LISTEN_AND_ARRANGE` | có | Nghe rồi xếp lại câu |
| `SPEAKING` | có | Câu mẫu người bản xứ đọc, để bắt chước |
| `TRANSLATE_TO_VN` | không | Đề bài đã in sẵn mặt chữ kèm phiên âm |
| `TRANSLATE_TO_JP` | không | Nghe là ra thẳng đáp án |
| `SELECT_IMAGE` | không | Đề bài là chữ, đáp án là ảnh |

Bỏ loa khỏi đề bài **không phải** là bỏ cách đọc. Từ nào chỉ xuất hiện ở câu dịch hoặc
câu chọn hình thì vẫn có file phát âm, chỉ là để ở `metadata_json.teachAudio` — trường mà
màn hình câu hỏi không đọc tới, chỉ pha **dạy trước bài** (`buildTeachCards` ở frontend)
mới dùng. Nhờ vậy 996/1.592 câu mang sẵn cách đọc cho phần giới thiệu từ mới, mà đề bài
vẫn sạch: chỉ 596 câu có nút loa, đúng những câu bắt buộc phải nghe.

**Hình ảnh không bao giờ được lộ đáp án.** Cụ thể, `LISTEN_AND_SELECT` **không đính ảnh**:
để ảnh con mèo cạnh câu "nghe và chọn từ đúng" là người học nhìn hình đoán ra ngay mà
chẳng cần nghe. Ảnh chỉ nằm ở chỗ an toàn: minh hoạ đề bài tiếng Việt của `TRANSLATE_TO_JP`,
minh hoạ nội dung câu ở `LISTEN_AND_ARRANGE` (đáp án là *thứ tự* các thẻ, hình không gợi ý
được thứ tự), và làm chính đáp án ở `SELECT_IMAGE`.

**Bốn thẻ ảnh của một câu chọn hình phải là bốn hình KHÁC NHAU.** Ảnh minh hoạ dựng từ
emoji, mà nhiều từ trong cùng chủ đề lại trùng emoji (11 từ số đếm đều là 🔢; ちち /
おとうさん / おとこのひと đều là 👨). `distinct_image_distractors` ép mỗi thẻ một emoji riêng, và
gom không đủ 3 đáp án nhiễu khác nhau thì bỏ hẳn dạng câu chọn hình cho từ đó — thà ít
ảnh còn hơn bốn thẻ giống hệt nhau không thẻ nào đúng hơn thẻ nào.

---

## 1c. Người học chưa biết chữ nào thì đọc bằng gì

Mọi mẩu chữ tiếng Nhật hiện trên màn hình đều đọc được:

- **Đáp án và thẻ rời có `metadata_json.romaji`** — frontend in thành dòng nhỏ ngay dưới
  chữ Nhật. Từ vựng và câu mẫu lấy romaji gõ tay trong `data_topics_*.py`; đáp án nhiễu và
  các thẻ rời của câu sắp xếp thì `romaji.py` tự chuyển (đã đối chiếu khớp 464/477 từ có
  romaji gõ tay, phần lệch là các trợ từ đọc lệch mặt chữ は/へ/を đã xử lý riêng).
- **Mỗi câu hỏi (cả 1.592 câu) có `metadata_json.glossary`** — từ điển mini `{kana: {r: romaji, v: nghĩa}}`
  chỉ chứa các từ xuất hiện trong chính câu đó. Frontend cho chạm giữ vào từ nào là hiện
  cách đọc và nghĩa của từ đó, không phải thoát ra tra từ điển ngoài.

---

## 2. Tài khoản demo

Mật khẩu **tất cả** tài khoản demo: `demo1234`

| Email | Tên hiển thị | Trạng thái | Dùng để demo |
|---|---|---|---|
| `moi@nihongo.app` | Trần Bảo An | Chưa học gì, 0 EXP, 25 năng lượng | Luồng người dùng mới từ con số 0 |
| `linh@nihongo.app` | Nguyễn Thùy Linh | Xong 2 chủ đề đầu, đang ở Số đếm bài 3. Streak 12, 860 xu, hạng Bạc | Lộ trình khoá/mở khoá, ngân hàng lỗi sai |
| `minh@nihongo.app` | Trần Quang Minh | Xong 5 chủ đề đầu, đang ở Thời gian và lịch. Hạng Vàng, đang có **Double XP** hiệu lực | Vật phẩm tăng lực, thăng hạng |
| `mai@nihongo.app` | Lê Ngọc Mai | Hạng Bạch Kim, streak 78, có đồ trong túi | Cửa hàng, túi đồ, trang bị khung avatar |
| `hung@nihongo.app` | Phạm Việt Hùng | Hạng Kim Cương, streak 126, 7.300 xu | Đỉnh bảng xếp hạng |
| `duc@nihongo.app` | Ngô Minh Đức | Còn 5 năng lượng, chưa học hôm nay | Hết năng lượng, nhắc giữ streak |
| 22 tài khoản khác | … | EXP rải đều từ 95 đến 14.800 | Bảng xếp hạng đủ người ở cả 5 hạng |

Số người mỗi hạng: Đồng 10 · Bạc 6 · Vàng 5 · Bạch Kim 5 · Kim Cương 4.

Tài khoản quản trị giữ nguyên: `admin@nihongo.app` / `admin123`.
Hai tài khoản cá nhân sẵn có trong máy (id 8, 9) **không bị đụng tới**, chỉ được nạp thêm xu.

---

## 3. Nạp dữ liệu

```bash
docker compose up -d                      # MySQL phải chạy trước
bash test-data/demo-seed/load_all.sh      # nạp tất cả, in ra bảng thống kê khi xong
./mvnw spring-boot:run
```

`load_all.sh` chạy 4 file theo đúng thứ tự bắt buộc:

| File | Nội dung |
|---|---|
| `out/seed.sql` | Xoá sạch nội dung học cũ rồi nạp 14 chủ đề / 100 bài / 1.592 câu hỏi |
| `out/seed_kana_audio.sql` | Gắn âm thanh cho cả 208 ký tự bảng chữ cái |
| `out/seed_shop_icons.sql` | Icon cho 12 vật phẩm cửa hàng |
| `out/seed_users.sql` | 28 học viên + tiến độ + lỗi sai + túi đồ |

Script **không đụng** vào bảng `shop_items`, `ranks`, `quest_definitions` (đã có seed sẵn
trong migration), và không xoá tài khoản nào có id < 100 ngoài 3 tài khoản test cũ
(`testbronze`, `testsilver`, `testgold`) đã được thay bằng học viên có tên thật.

### Chia sẻ cho người khác trong nhóm

`uploads/` **nằm trong `.gitignore`**, nên 1.329 file ảnh/âm thanh không đi theo git. Người
khác pull code về sẽ có đủ SQL nhưng thiếu media. Có 2 cách:

- **Cách gọn (khuyến nghị)**: người đó chạy `python make_media.py` — script tự tạo lại toàn bộ
  ảnh và âm thanh y hệt (mất khoảng 5 phút, cần Internet cho phần đọc tiếng Nhật).
- **Cách nhanh**: nén thư mục `uploads/` (khoảng 15 MB) gửi trực tiếp, giải nén vào đúng chỗ.

### Tạo lại từ đầu (khi muốn sửa nội dung)

```bash
cd test-data/demo-seed
python build.py          # sinh lại seed.sql + media.json
python build_users.py    # sinh lại seed_users.sql
python make_media.py     # tạo ảnh/âm thanh còn thiếu (bỏ qua file đã có)
cd ../.. && bash test-data/demo-seed/load_all.sh
```

Yêu cầu: `pip install edge-tts pillow bcrypt`. Random đã cố định seed nên chạy lại cho
kết quả giống hệt.

| File nguồn | Sửa gì ở đây |
|---|---|
| `data_kana.py` | Bảng chữ cái — hiện KHÔNG dùng nữa (lộ trình đã bỏ 2 chủ đề mặt chữ) |
| `data_topics_a.py` | Chủ đề 1–5: từ vựng, câu mẫu, giải thích ngữ pháp |
| `data_topics_b.py` | Chủ đề 6–10: tương tự |
| `data_topics_c.py` | Chủ đề 11–14 (mua sắm, đi lại, sở thích, sức khoẻ) |
| `romaji.py` | Chuyển kana -> romaji cho những chỗ không có romaji gõ tay |
| `build.py` | Cách sinh câu hỏi từ dữ liệu (dạng câu, đáp án nhiễu, cấu hình bài) |
| `build_users.py` | Danh sách học viên demo và trạng thái của họ |
| `make_media.py` | Cách vẽ ảnh và giọng đọc TTS |

---

## 4. Kịch bản demo gợi ý (khoảng 10 phút)

1. **Người học mới** — đăng nhập `moi@nihongo.app`, mở `GET /api/v1/topics`: chỉ bài 1 của
   chủ đề Chào hỏi hằng ngày mở, phần còn lại khoá. Học bài 1 (`/lessons/1/start` →
   `/submit`) và cho thấy bài 2 mở khoá ngay sau đó. Ngay câu đầu tiên đã là một câu chào
   dùng được thật, không phải một ký tự rời.
2. **Bài học có tiếng và hình** — trong response `/start`, câu `LISTEN_AND_SELECT` có
   `audioUrl` phát được ngay ở trình duyệt
   (`http://localhost:8080/uploads/audios/words/konnichiwa.mp3`), còn câu dịch thì
   `audioUrl` là `null` — đúng như thiết kế, xem mục 1b. Câu `SELECT_IMAGE` có 4 thẻ ảnh
   khác nhau và không thẻ nào lộ chữ.
3. **Người đang học dở** — `linh@nihongo.app`: lộ trình hiện 2 chủ đề đầu đã xong hết,
   Số đếm mới tới bài 3, các chủ đề sau còn khoá.
4. **Ngân hàng lỗi sai** — vẫn tài khoản Linh: `/api/v1/reviews/mistakes/summary` cho thấy số
   câu đang nợ, `/start` trả về phiên ôn tập **không lộ đáp án** và không tốn năng lượng.
5. **Bảng chữ cái** — `/api/v1/alphabets?type=HIRAGANA`: 26 nhóm, mỗi ký tự có âm thanh và
   mức thông thạo riêng của người học; `/alphabets/practice/start` sinh đề luyện tập.
6. **Gamification** — `/users/me` (EXP, xu, streak, hạng), `/users/me/quests` (nhiệm vụ ngày
   tự sinh), `/users/me/shop` (12 vật phẩm có icon), `/leaderboard` (xếp hạng theo hạng đấu).
7. **Thi vượt cấp** — bài `JUMP_TEST` ở đầu chủ đề 1 luôn mở: làm đạt sẽ đánh dấu hoàn
   thành toàn bộ chủ đề, dành cho người đã giao tiếp cơ bản được. Chủ đề cuối cũng có một
   bài tương tự để kiểm tra trình độ N5.
8. **Hết năng lượng** — `duc@nihongo.app` chỉ còn 5 năng lượng: vào bài thường (tốn 10) sẽ bị
   chặn, minh hoạ cơ chế giới hạn học mỗi ngày.

---

## 5. Lưu ý kỹ thuật

- **Phần giải thích ngữ pháp của mỗi bài nằm trong `lessons.config_json.description`**, vì
  bảng `lessons` không có cột `description` (chỉ `topics` có). API admin
  `GET /api/v1/admin/lessons/{id}` trả về `configJson` nên đọc được; còn API lộ trình
  `GET /api/v1/topics` thì cố tình không trả `configJson` cho nhẹ payload. Nếu muốn hiển thị
  phần giảng giải này trên app, cần thêm cột `description` cho `lessons` (một migration nhỏ)
  hoặc cho `RoadmapLessonResponse` trả kèm.
- Đường dẫn media lưu **tương đối** (`/uploads/...`) nên đổi domain/IP không phải sửa dữ liệu.
  App phục vụ chúng qua `WebConfig` từ thư mục `uploads/`. Khi chạy trên điện thoại thật, nhớ
  trỏ base URL về IP LAN của máy chạy backend.
- Cấu hình từng loại bài (`config_json`): bài chữ cái tốn 5 năng lượng / +15 EXP, bài từ vựng
  10 năng lượng / +20 EXP, bài ôn tập 5 năng lượng / +30 EXP với mốc sao [90s, 150s], bài thi
  vượt cấp 15 năng lượng / +80 EXP. Mỗi lần vào học lấy ngẫu nhiên 10 câu từ kho 11–26 câu của
  bài, nên học lại lần hai vẫn ra đề khác.
- Câu `LISTEN_AND_ARRANGE` giữ đúng quy ước cũ của dự án: mọi thẻ đều `isCorrect = true`,
  `orderIndex` là thứ tự đúng trong câu; frontend tự chấm theo thứ tự người dùng ghép.
- **Lệch múi giờ giữa MySQL và ứng dụng**: container MySQL chạy giờ UTC, còn app đọc/ghi theo
  `Asia/Ho_Chi_Minh` (khai trong `serverTimezone` của `application.yml`). Nếu seed dùng thẳng
  `NOW()` của MySQL thì mọi mốc thời gian bị lùi 7 tiếng, và `EnergyService` sẽ tưởng đã trôi
  qua 7 giờ nên **hồi đầy năng lượng ngay lần gọi API đầu tiên** — làm hỏng kịch bản demo
  "hết năng lượng". `seed_users.sql` vì vậy dùng biến `@vn_now = CONVERT_TZ(NOW(),'+00:00','+07:00')`.
  Đây là điểm cần nhớ cho mọi script SQL viết tay sau này.
- Câu `SELECT_IMAGE` có `optionText = null`, nhãn tiếng Việt nằm trong `metadataJson.label`
  để frontend hiện sau khi người học đã chọn.
