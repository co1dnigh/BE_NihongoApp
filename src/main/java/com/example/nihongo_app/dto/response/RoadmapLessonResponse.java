package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.Lesson.LessonType;
import lombok.Builder;
import lombok.Value;

/**
 * DTO "mỏng" phục vụ riêng cho GET /api/v1/topics (vẽ bản đồ lộ trình).
 *
 * <p>Không trả về configJson, createdAt, updatedAt, danh sách câu hỏi để payload
 * JSON gọn nhẹ cho thiết bị di động.</p>
 */
@Value
@Builder
public class RoadmapLessonResponse {

    /** Trạng thái hiển thị trên bản đồ (đã được tính toán từ lộ trình tổng thể). */
    public enum Status {
        LOCKED, UNLOCKED, COMPLETED
    }

    Long lessonId;
    String title;
    LessonType lessonType;
    Integer orderIndex;
    Status status;
    Integer starsEarned;
    /** So nang luong bi tru khi bat dau bai nay -- de ban do bao truoc cho nguoi hoc. */
    Integer entryCostEnergy;
}
