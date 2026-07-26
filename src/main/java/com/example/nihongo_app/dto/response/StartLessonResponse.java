package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.Lesson.LessonType;
import com.example.nihongo_app.entity.LessonQuestion.QuestionType;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.JsonNode;

/**
 * Response cho {@code POST /api/v1/lessons/{id}/start}.
 *
 * <p>Trả về bộ đề thi đầy đủ (câu hỏi + đáp án) đã được shuffle, kèm thông tin
 * năng lượng đã trừ để frontend cập nhật UI.</p>
 */
@Value
@Builder
public class StartLessonResponse {

    Long lessonId;
    LessonType lessonType;

    /**
     * Tổng năng lượng đã trừ lúc bắt đầu bài.
     * Frontend dùng để trừ UI energy bar ngay lập tức (kể cả khi user chưa nộp bài).
     */
    Integer totalEnergyDeducted;

    /**
     * {@code true} nếu user đang làm lại bài đã hoàn thành trước đó (replay).
     * Lúc này {@code totalEnergyDeducted = 0} và phần thưởng EXP khi nộp bài sẽ
     * thấp hơn lần đầu (theo {@code configJson.replayExpRatio}, mặc định 30%).
     * FE có thể dùng flag này để hiển thị UI hint "Làm lại - thưởng giảm".
     */
    Boolean isReplay;

    /**
     * Danh sách câu hỏi đã shuffle (đã trộn cả thứ tự câu lẫn thứ tự đáp án).
     * Mỗi câu kèm metadata đầy đủ để FE tự chấm realtime bằng {@code options[].isCorrect}.
     */
    List<StartLessonQuestion> questions;

    @Value
    @Builder
    public static class StartLessonQuestion {
        Long questionId;
        QuestionType questionType;
        String content;
        String audioUrl;
        String imageUrl;
        JsonNode metadataJson;
        List<StartLessonOption> options;
    }

    @Value
    @Builder
    public static class StartLessonOption {
        Long optionId;
        String content;
        String imageUrl;
        String audioUrl;
        JsonNode metadataJson;
        /**
         * Đáp án đúng. FE dùng để chấm realtime; user hoàn toàn có thể thấy
         * flag này nếu inspect payload, nhưng đây là thiết kế cố ý theo spec
         * (FE phụ trách chấm, BE chỉ verify lại ở /submit khi cần thiết).
         */
        Boolean isCorrect;
        /**
         * Thứ tự đúng của block trong câu ARRANGE_WORDS.
         * {@code null} nếu option không thuộc dạng sắp xếp.
         */
        Integer order;
    }
}