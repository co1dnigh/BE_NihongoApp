package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * DTO trả về cho GET /api/v1/topics. Cấu trúc cây (topic → lessons) để frontend
 * dễ dàng vẽ bản đồ lộ trình theo từng chủ đề.
 */
@Value
@Builder
public class RoadmapTopicResponse {

    Long topicId;
    String topicTitle;
    List<RoadmapLessonResponse> lessons;
}
