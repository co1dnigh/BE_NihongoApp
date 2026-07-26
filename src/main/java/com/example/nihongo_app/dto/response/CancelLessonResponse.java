package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Response cho {@code POST /api/v1/lessons/{id}/cancel}.
 *
 * <p>FE gọi khi user bấm Back mà chưa làm câu nào. BE sẽ hoàn lại năng lượng đã trừ
 * lúc /start. Trả về năng lượng hiện tại để FE cập nhật UI ngay.</p>
 */
@Value
@Builder
public class CancelLessonResponse {

    /** Năng lượng đã hoàn lại cho user (bằng số đã trừ lúc /start). */
    Integer energyRefunded;

    /** Năng lượng hiện tại của user sau khi hoàn. */
    Integer currentEnergy;

    /** Trạng thái bài học sau khi huỷ: luôn là {@code LOCKED} (chưa được tính là đã bắt đầu). */
    String status;
}