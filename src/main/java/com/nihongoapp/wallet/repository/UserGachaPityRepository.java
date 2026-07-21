package com.nihongoapp.wallet.repository;

import com.nihongoapp.wallet.model.UserGachaPity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface UserGachaPityRepository extends JpaRepository<UserGachaPity, Long> {

 @Query("SELECT p FROM UserGachaPity p WHERE p.id.userId = :userId AND p.id.bannerId = :bannerId")
 Optional<UserGachaPity> findByUserIdAndBannerId(@Param("userId") Long userId, @Param("bannerId") Long bannerId);

 @Query("SELECT p FROM UserGachaPity p WHERE p.id.userId = :userId AND p.id.bannerId = :bannerId")
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 Optional<UserGachaPity> findWithPessimisticLockByUserIdAndBannerId(@Param("userId") Long userId, @Param("bannerId") Long bannerId);
}
