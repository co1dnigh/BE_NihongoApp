package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.QuestDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestDefinitionRepository extends JpaRepository<QuestDefinition, Long> {

    List<QuestDefinition> findAllByActiveTrue();
}
