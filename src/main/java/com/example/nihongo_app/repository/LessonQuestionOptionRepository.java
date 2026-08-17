package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.LessonQuestionOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonQuestionOptionRepository extends JpaRepository<LessonQuestionOption, Long> {

    /**
     * Lấy toàn bộ đáp án của 1 câu hỏi, sắp theo {@code orderIndex ASC} (null cuối),
     * tie-break bằng {@code id ASC} để giữ thứ tự ổn định khi shuffle.
     */
    List<LessonQuestionOption> findAllByQuestionIdOrderByOrderIndexAscIdAsc(Long questionId);

    List<LessonQuestionOption> findAllByQuestionIdOrderByOrderIndexAsc(Long questionId);

    void deleteAllByQuestionId(Long questionId);

    /** Đáp án đúng của 1 câu hỏi — dùng để trả về cho FE sau khi chấm (vd phiên ôn lỗi sai). */
    Optional<LessonQuestionOption> findFirstByQuestionIdAndCorrectTrue(Long questionId);
}
