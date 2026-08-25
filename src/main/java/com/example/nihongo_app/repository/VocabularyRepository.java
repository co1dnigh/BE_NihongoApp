package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    /**
     * Các từ TRỌNG TÂM của một loạt câu hỏi — tức những từ mà trả lời đúng câu đó
     * thật sự chứng minh là người học nhớ được.
     *
     * <p>Từ phụ trợ ({@code is_target = false}) cố tình bị loại: chúng chỉ có mặt để
     * người học bấm giữ tra nghĩa. Trả lời đúng một câu KHÔNG có nghĩa là đã thuộc mọi
     * từ xuất hiện quanh nó — cộng điểm cho chúng sẽ thổi phồng tiến độ và đẩy lịch ôn
     * của những từ chưa hề được kiểm tra ra xa.</p>
     */
    @Query(value = """
            SELECT qv.question_id AS questionId, v.id AS vocabularyId
            FROM question_vocabulary qv
            JOIN vocabulary v ON v.id = qv.vocabulary_id
            WHERE qv.is_target = TRUE AND qv.question_id IN (:questionIds)
            """, nativeQuery = true)
    List<Object[]> findTargetVocabularyByQuestionIds(@Param("questionIds") List<Long> questionIds);

    /**
     * Chiều ngược của {@link #findTargetVocabularyByQuestionIds}: cho một tập từ vựng, tìm
     * các câu hỏi NHẮM TỚI (is_target = TRUE) chính những từ đó, giới hạn trong các bài
     * NORMAL thuộc các topic được chỉ định.
     *
     * <p>Dùng để dựng bài ôn tập TOPIC_REVIEW theo lịch SM-2: đầu vào là các từ đang đến
     * hạn ôn của user, đầu ra là câu hỏi thật (đã có sẵn nội dung, đáp án) từng dạy từ đó —
     * thay vì phải nhân bản câu hỏi tĩnh lúc seed.</p>
     */
    @Query(value = """
            SELECT qv.vocabulary_id AS vocabularyId, qv.question_id AS questionId
            FROM question_vocabulary qv
            JOIN lesson_questions lq ON lq.id = qv.question_id
            JOIN lessons l ON l.id = lq.lesson_id
            WHERE qv.is_target = TRUE
              AND qv.vocabulary_id IN (:vocabularyIds)
              AND l.lesson_type = 'NORMAL'
              AND l.topic_id IN (:topicIds)
            """, nativeQuery = true)
    List<Object[]> findCandidateQuestionsForVocabulary(
            @Param("vocabularyIds") List<Long> vocabularyIds,
            @Param("topicIds") List<Long> topicIds);

    /** Toàn bộ kho từ để client cache lại làm từ điển tra tại chỗ. */
    List<Vocabulary> findAllByOrderByIdAsc();
}
