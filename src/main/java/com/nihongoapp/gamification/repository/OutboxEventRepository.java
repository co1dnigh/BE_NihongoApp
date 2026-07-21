package com.nihongoapp.gamification.repository;

import com.nihongoapp.gamification.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'pending' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAt(@Param("status") String status);
}
