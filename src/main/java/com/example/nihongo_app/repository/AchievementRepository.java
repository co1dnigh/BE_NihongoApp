package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Achievement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    /** Lấy achievement đang active theo code (dùng check-on-event). */
    Optional<Achievement> findByCodeAndActiveTrue(String code);

    /** Danh sách achievement đang active (FE hiện danh sách). */
    List<Achievement> findAllByActiveTrue();
}