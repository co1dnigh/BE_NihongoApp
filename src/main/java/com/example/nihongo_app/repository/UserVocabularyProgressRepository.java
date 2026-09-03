package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.UserVocabularyProgress;
import com.example.nihongo_app.entity.UserVocabularyProgressId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.nihongo_app.entity.Vocabulary;

public interface UserVocabularyProgressRepository
        extends JpaRepository<UserVocabularyProgress, UserVocabularyProgressId> {

    List<UserVocabularyProgress> findAllByUserIdAndVocabularyIdIn(Long userId, List<Long> vocabularyIds);

    /**
     * Những từ đã tới hạn ôn, từ quá hạn lâu nhất trước.
     *
     * <p>Sắp theo {@code nextDueAt} tăng dần chứ không phải ngẫu nhiên: từ quá hạn càng
     * lâu thì xác suất đã quên càng cao, ôn trước là đúng thứ tự ưu tiên.</p>
     */
    List<UserVocabularyProgress> findAllByUserIdAndNextDueAtLessThanEqualOrderByNextDueAtAsc(
            Long userId, LocalDateTime now, Pageable pageable);

    long countByUserIdAndNextDueAtLessThanEqual(Long userId, LocalDateTime now);

    /**
     * Từ đã học nhưng CHƯA tới hạn, gần tới hạn nhất trước — dùng để độn thêm bài ôn tập
     * (TOPIC_REVIEW) khi số từ thật sự đến hạn chưa đủ lấp đầy 1 phiên.
     */
    List<UserVocabularyProgress> findAllByUserIdAndFirstLearnedAtIsNotNullAndNextDueAtAfterOrderByNextDueAtAsc(
            Long userId, LocalDateTime now, Pageable pageable);

    /** Sổ tay "từ đã học": mọi từ người dùng từng gặp, mới nhất trước. */
    List<UserVocabularyProgress> findAllByUserIdAndFirstLearnedAtIsNotNullOrderByFirstLearnedAtDesc(
            Long userId, Pageable pageable);

    long countByUserIdAndFirstLearnedAtIsNotNull(Long userId);

    @Query("SELECT p FROM UserVocabularyProgress p JOIN Vocabulary v ON p.vocabularyId = v.id WHERE p.userId = :userId AND p.nextDueAt <= :now AND v.itemType IN :itemTypes ORDER BY p.nextDueAt ASC")
    List<UserVocabularyProgress> findAllByUserIdAndNextDueAtLessThanEqualAndItemTypeInOrderByNextDueAtAsc(
            @Param("userId") Long userId, @Param("now") LocalDateTime now, @Param("itemTypes") List<Vocabulary.ItemType> itemTypes, Pageable pageable);

    @Query("SELECT COUNT(p) FROM UserVocabularyProgress p JOIN Vocabulary v ON p.vocabularyId = v.id WHERE p.userId = :userId AND p.nextDueAt <= :now AND v.itemType IN :itemTypes")
    long countByUserIdAndNextDueAtLessThanEqualAndItemTypeIn(
            @Param("userId") Long userId, @Param("now") LocalDateTime now, @Param("itemTypes") List<Vocabulary.ItemType> itemTypes);

    @Query("SELECT COUNT(p) FROM UserVocabularyProgress p JOIN Vocabulary v ON p.vocabularyId = v.id WHERE p.userId = :userId AND p.firstLearnedAt IS NOT NULL AND v.itemType IN :itemTypes")
    long countByUserIdAndFirstLearnedAtIsNotNullAndItemTypeIn(
            @Param("userId") Long userId, @Param("itemTypes") List<Vocabulary.ItemType> itemTypes);
}
