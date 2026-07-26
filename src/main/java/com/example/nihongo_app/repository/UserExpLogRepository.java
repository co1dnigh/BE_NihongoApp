package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.UserExpLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExpLogRepository extends JpaRepository<UserExpLog, Long> {
}