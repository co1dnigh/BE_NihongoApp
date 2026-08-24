package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Envelope chung cho mọi danh sách phân trang kiểu cursor trong module Social Feed
 * (feed, followers/following, comments, search). Dùng chung 1 shape để FE xử lý nhất quán.
 */
@Value
@Builder
public class CursorPageResponse<T> {

    List<T> items;

    /** {@code null} nếu đã hết trang — FE dừng gọi thêm khi thấy giá trị này. */
    String nextCursor;
}
