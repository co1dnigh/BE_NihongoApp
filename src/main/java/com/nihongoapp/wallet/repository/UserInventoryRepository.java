package com.nihongoapp.wallet.repository;

import com.nihongoapp.wallet.model.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {

    @Modifying
    @Query(value = "INSERT INTO user_inventory (user_id, item_type, gacha_item_id, quantity) " +
                   "VALUES (:userId, :itemType, :gachaItemId, :qty) " +
                   "ON DUPLICATE KEY UPDATE quantity = quantity + :qty",
                   nativeQuery = true)
    int insertOrAddQuantity(@Param("userId") Long userId,
                            @Param("itemType") String itemType,
                            @Param("gachaItemId") Long gachaItemId,
                            @Param("qty") Integer qty);

    java.util.Optional<UserInventory> findByUserIdAndItemType(Long userId, UserInventory.ItemType itemType);
}
