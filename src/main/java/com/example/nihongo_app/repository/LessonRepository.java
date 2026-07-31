package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Lấy toàn bộ lesson thuộc cùng 1 topic, sắp theo {@code orderIndex} tăng dần
     * (JUMP_TEST có {@code orderIndex = null} sẽ rơi xuống cuối nhờ {@code NULLS LAST}).
     *
     * <p>Dùng cho {@code LessonUnlockPolicy} khi cần xét 1 bài lẻ: helper này sẽ
     * tái sử dụng cùng thuật toán với {@code RoadmapServiceImpl} để đảm bảo
     * "trước - sau" nhất quán.</p>
     */
    @Query("""
            SELECT l
            FROM Lesson l
            WHERE l.topicId = :topicId
            ORDER BY
                CASE WHEN l.orderIndex IS NULL THEN 1 ELSE 0 END ASC,
                l.orderIndex ASC,
                l.id ASC
            """)
    List<Lesson> findAllByTopicIdOrdered(@Param("topicId") Long topicId);

    @Query("""
            SELECT l
            FROM Lesson l
            JOIN l.topic t
            WHERE t.deletedAt IS NULL
            ORDER BY
                t.orderIndex ASC,
                CASE WHEN l.orderIndex IS NULL THEN 1 ELSE 0 END ASC,
                l.orderIndex ASC,
                l.id ASC
            """)
    List<Lesson> findAllActiveOrdered();
}
