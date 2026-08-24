package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.UserStreakDay;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Ghi nhận ngày user đã học (streak calendar / heat map).
 */
public interface UserStreakDayRepository extends JpaRepository<UserStreakDay, Long> {

    /** Kiểm tra user đã học ngày đó chưa (dùng để upsert idempotent). */
    boolean existsByUserIdAndStudyDate(Long userId, LocalDate studyDate);

    /** Danh sách ngày user đã học trong khoảng [from, to]. */
    List<UserStreakDay> findAllByUserIdAndStudyDateBetween(
            Long userId, LocalDate from, LocalDate to);

    /** Đếm số ngày user đã học trong khoảng (dùng cho streak calendar). */
    @Query("SELECT COUNT(u) FROM UserStreakDay u "
            + "WHERE u.userId = :userId AND u.studyDate BETWEEN :from AND :to")
    long countByUserIdAndStudyDateBetween(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** Lấy ngày học mới nhất của user (dùng để tính streak khi cần). */
    @Query("SELECT u.studyDate FROM UserStreakDay u "
            + "WHERE u.userId = :userId ORDER BY u.studyDate DESC")
    List<LocalDate> findStudyDatesByUserIdDesc(Long userId);
}