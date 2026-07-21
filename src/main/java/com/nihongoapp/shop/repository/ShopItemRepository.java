package com.nihongoapp.shop.repository;

import com.nihongoapp.shop.model.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    Optional<ShopItem> findByIdAndIsActiveTrue(Long id);
}
