package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.UserDailyQuest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDailyQuestRepository extends JpaRepository<UserDailyQuest, Long> {

    List<UserDailyQuest> findAllByUserIdAndQuestDate(Long userId, LocalDate questDate);
}
