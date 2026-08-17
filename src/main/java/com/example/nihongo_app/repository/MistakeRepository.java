package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Mistake;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MistakeRepository extends JpaRepository<Mistake, Long> {

    Optional<Mistake> findByUserIdAndQuestionId(Long userId, Long questionId);

    long countByUserIdAndStatus(Long userId, Mistake.Status status);

    /**
     * Lấy tối đa {@code pageable.getPageSize()} mistake của user theo status, ưu tiên
     * wrong_count cao nhất rồi tới last_wrong_at gần nhất — dùng để chọn đề cho phiên ôn.
     */
    List<Mistake> findAllByUserIdAndStatusOrderByWrongCountDescLastWrongAtDesc(
            Long userId, Mistake.Status status, Pageable pageable);
}
