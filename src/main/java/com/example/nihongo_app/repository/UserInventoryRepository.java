package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.UserInventory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {

List<UserInventory> findAllByUserId(Long userId);

List<UserInventory> findByUserIdAndEquippedTrue(Long userId);

@Query("SELECT ui FROM UserInventory ui WHERE ui.userId = :userId AND ui.itemId = :itemId")
Optional<UserInventory> findByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);


@Query("SELECT ui FROM UserInventory ui WHERE ui.userId = :userId AND ui.equipped = true AND ui.itemId IN (SELECT si.id FROM ShopItem si WHERE si.effectType = :effectType)")
List<UserInventory> findEquippedCosmeticByUserIdAndEffectType(@Param("userId") Long userId, @Param("effectType") ShopItem.EffectType effectType);
}
