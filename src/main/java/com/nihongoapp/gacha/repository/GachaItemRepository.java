package com.nihongoapp.gacha.repository;

import com.nihongoapp.gacha.model.GachaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GachaItemRepository extends JpaRepository<GachaItem, Long> {
    List<GachaItem> findByBannerId(Long bannerId);
}
