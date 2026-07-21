package com.nihongoapp.gacha.repository;

import com.nihongoapp.gacha.model.GachaBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GachaBannerRepository extends JpaRepository<GachaBanner, Long> {
    Optional<GachaBanner> findByIdAndIsActiveTrue(Long id);
}
