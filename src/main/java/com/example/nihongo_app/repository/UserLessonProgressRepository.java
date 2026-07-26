package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.UserLessonProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {

    /**
     * Lấy toàn bộ lịch sử học của 1 user trong 1 câu query duy nhất.
     * Service sẽ chuyển danh sách này thành {@code Map<Long, UserLessonProgress>} để tra cứu O(1).
     */
    List<UserLessonProgress> findAllByUserId(Long userId);

    /**
     * Tìm progress của user cho đúng 1 lesson, dùng trong upsert (Start / Submit).
     */
    Optional<UserLessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
}
