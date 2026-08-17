package com.example.nihongo_app.dto.request;

import lombok.Data;

/**
 * 1 câu trả lời của user: câu hỏi nào, chọn option nào. Dùng chung cho
 * {@link SubmitLessonRequest} (nộp bài học thường) và phiên ôn lỗi sai
 * ({@code ReviewSubmitRequest}) — BE luôn tự đối chiếu {@code selectedOptionId}
 * với DB để xác định đúng/sai, không nhận is_correct từ FE.
 */
@Data
public class AnswerItem {

    private Long questionId;

    private Long selectedOptionId;
}
