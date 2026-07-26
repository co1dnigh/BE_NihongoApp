package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Topic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    /**
     * Lấy toàn bộ Topic đang active (chưa xoá mềm) cùng toàn bộ Lesson thuộc Topic
     * chỉ bằng 1 câu query duy nhất (tránh N+1).
     *
     * <p>{@code DISTINCT} cần thiết vì {@code JOIN FETCH} với quan hệ OneToMany sẽ
     * sinh ra Cartesian product, làm trùng {@code Topic} trong kết quả.</p>
     *
     * <p>Sắp xếp theo {@code topic.orderIndex} trước, sau đó {@code lesson.orderIndex}
     * để frontend vẽ bản đồ theo đúng thứ tự.</p>
     */
    @Query("""
            SELECT DISTINCT t
            FROM Topic t
            JOIN FETCH t.lessons l
            WHERE t.deletedAt IS NULL
            ORDER BY t.orderIndex ASC, l.orderIndex ASC
            """)
    List<Topic> findAllActiveWithLessons();
}
