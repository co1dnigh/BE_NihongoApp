package com.example.nihongo_app.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Mã hoá/giải mã cursor cho phân trang kiểu cursor-based dùng chung trong module Social Feed
 * (feed, followers/following, comment, search) — encode nhiều phần (vd thời điểm + id) thành
 * 1 chuỗi Base64 duy nhất để trả cho FE, tránh lộ trực tiếp giá trị DB ra URL.
 */
final class CursorCodec {

    // Ky tu phan cach cac phan cua cursor. Du lieu thuc te (timestamp ISO-8601, id so,
    // display name) khong dung dau ~, nen an toan de tach lai chinh xac.
    private static final String DELIMITER = "~";

    private CursorCodec() {
    }

    static String encode(String... parts) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(String.join(DELIMITER, parts).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Giải mã cursor thành các phần đã encode. Trả về {@code null} nếu cursor rỗng hoặc
     * không hợp lệ (client tự sửa/gửi sai) — coi như trang đầu tiên thay vì báo lỗi.
     */
    static String[] decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            return decoded.split(DELIMITER, -1);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
