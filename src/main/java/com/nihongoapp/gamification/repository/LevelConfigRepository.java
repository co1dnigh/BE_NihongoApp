package com.nihongoapp.gamification.repository;

import com.nihongoapp.gamification.model.LevelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LevelConfigRepository extends JpaRepository<LevelConfig, Integer> {
}
