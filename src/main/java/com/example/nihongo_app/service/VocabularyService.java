package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.VocabularyReviewRequest;
import com.example.nihongo_app.dto.response.VocabularyDueResponse;
import com.example.nihongo_app.dto.response.VocabularyReviewResponse;
import com.example.nihongo_app.dto.response.VocabularyItemResponse;
import com.example.nihongo_app.entity.LessonAttemptAnswer;

import java.util.List;

/**
 * Kho từ vựng dùng chung + lịch ôn tập ngắt quãng.
 *
 * <p>Một nguồn dữ liệu phục vụ ba tính năng: tra nghĩa khi bấm giữ vào chữ Nhật,
 * sổ tay "từ đã học", và hàng đợi ôn tập theo SM-2.</p>
 */
public interface VocabularyService {

    /**
     * Ghi nhận kết quả một lượt làm bài vào lịch ôn tập.
     *
     * <p>Gọi ngay sau khi server đã chấm xong, trong CÙNG transaction với việc nộp bài:
     * lịch ôn mà lệch với kết quả đã ghi thì người học sẽ được hỏi lại những từ họ vừa
     * trả lời đúng, hoặc tệ hơn là không bao giờ được hỏi lại từ vừa sai.</p>
     */
    void recordFromAnswers(Long userId, List<LessonAttemptAnswer> gradedAnswers);

    /**
     * Áp kết quả một phiên ôn từ vựng vào lịch.
     *
     * <p>Khác {@link #recordFromAnswers}: ở đây client gửi thẳng theo TỪ chứ không qua
     * câu hỏi của bài học, vì phiên ôn được dựng từ hàng đợi tới hạn chứ không thuộc
     * bài nào cả.</p>
     */
    VocabularyReviewResponse submitReview(Long userId, VocabularyReviewRequest request);

    /**
     * Với mỗi câu hỏi: từ trọng tâm của nó có phải từ người học CHƯA TỪNG GẶP không.
     *
     * <p>Câu không gắn từ nào thì không có mặt trong kết quả — gọi bên gọi tự quyết
     * định coi đó là gì (hiện coi là không mới, vì không có căn cứ để khoe "từ mới").</p>
     */
    java.util.Map<Long, Boolean> resolveNewQuestions(Long userId, List<Long> questionIds);

    /** Những từ đã tới hạn ôn, kèm số đếm cho badge. */
    VocabularyDueResponse getDue(Long userId, int limit);

    /** Sổ tay: các từ người học đã gặp, mới nhất trước. */
    VocabularyDueResponse getLearned(Long userId, int limit);

    /** Những câu phát âm đã tới hạn ôn */
    VocabularyDueResponse getPronunciationDue(Long userId, int limit);

    /** Sổ tay: các câu phát âm đã học */
    VocabularyDueResponse getPronunciationLearned(Long userId, int limit);

    /**
     * Toàn bộ kho từ để client cache làm từ điển tra tại chỗ.
     *
     * <p>Nhờ bảng này mà MỌI chữ Nhật trên màn hình đều bấm giữ ra nghĩa được, thay vì
     * chỉ những từ nằm trong glossary mà người soạn bài nhớ điền cho riêng câu đó.</p>
     */
    List<VocabularyItemResponse> getGlossary();
}
