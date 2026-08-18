package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.LessonQuestion.QuestionType;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.JsonNode;

/**
 * Response cho {@code POST /api/v1/reviews/mistakes/start}.
 *
 * <p>Khác {@code StartLessonResponse}: không trừ năng lượng, và option KHÔNG kèm cờ đáp án
 * đúng (server tự chấm ở {@code /submit}, không để lộ đáp án lúc làm bài ôn tập).</p>
 */
@Value
@Builder
public class ReviewSessionResponse {

    /** Danh sách câu hỏi đã shuffle (rỗng nếu user không có mistake ACTIVE nào). */
    List<ReviewQuestion> questions;

    /** Thông báo ngắn cho FE hiển thị, đặc biệt khi {@code questions} rỗng. */
    String message;

    @Value
    @Builder
    public static class ReviewQuestion {
        Long questionId;
        QuestionType questionType;
        String content;
        String audioUrl;
        String imageUrl;
        JsonNode metadataJson;
        List<ReviewOption> options;
    }

    @Value
    @Builder
    public static class ReviewOption {
        Long optionId;
        String content;
        String imageUrl;
        String audioUrl;
        JsonNode metadataJson;
        Integer order;
    }
}
