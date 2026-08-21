package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.UserCharacterProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCharacterProgressRepository extends JpaRepository<UserCharacterProgress, Long> {

    List<UserCharacterProgress> findAllByUserId(Long userId);

    Optional<UserCharacterProgress> findByUserIdAndCharacterId(Long userId, Long characterId);
}