package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
