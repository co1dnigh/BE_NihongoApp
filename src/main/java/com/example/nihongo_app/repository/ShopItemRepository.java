package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.ShopItem;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {

    List<ShopItem> findAllByActiveTrueOrderBySortOrderAsc();

    List<ShopItem> findByItemTypeAndActiveTrueOrderBySortOrderAsc(ShopItem.ItemType itemType);

    @Query("SELECT s FROM ShopItem s WHERE s.id = :id AND s.active = true")
    Optional<ShopItem> findByIdAndActiveTrue(@Param("id") Long id);

    List<ShopItem> findByIdIn(Collection<Long> ids);
}