package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.UserActiveEffect;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActiveEffectRepository extends JpaRepository<UserActiveEffect, Long> {

    List<UserActiveEffect> findByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);

    boolean existsByUserIdAndEffectTypeAndExpiresAtAfter(Long userId, ShopItem.EffectType effectType, LocalDateTime now);
}
