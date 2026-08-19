package com.example.nihongo_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nihongo_app.dto.response.ConsumeItemResponse;
import com.example.nihongo_app.dto.response.InventoryItemResponse;
import com.example.nihongo_app.dto.response.ShopItemResponse;
import com.example.nihongo_app.dto.response.ShopPurchaseResponse;
import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserInventory;
import com.example.nihongo_app.entity.UserActiveEffect;
import com.example.nihongo_app.exception.InsufficientCoinsException;
import com.example.nihongo_app.exception.InventoryItemNotFoundException;
import com.example.nihongo_app.exception.ItemNotAvailableException;
import com.example.nihongo_app.exception.ResourceNotFoundException;
import com.example.nihongo_app.repository.CoinTransactionRepository;
import com.example.nihongo_app.repository.ShopItemRepository;
import com.example.nihongo_app.repository.UserActiveEffectRepository;
import com.example.nihongo_app.repository.UserInventoryRepository;
import com.example.nihongo_app.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

@Mock
private ShopItemRepository shopItemRepository;

@Mock
private UserInventoryRepository userInventoryRepository;

@Mock
private UserRepository userRepository;

    @Mock
    private CoinTransactionRepository coinTransactionRepository;

    @Mock
    private UserActiveEffectRepository userActiveEffectRepository;

@InjectMocks
private ShopService shopService;

private static final Long USER_ID = 1L;
private static final Long ITEM_ID = 1L;
private static final Long INVENTORY_ID = 10L;

private User user;
private ShopItem streakFreezeItem;
private ShopItem doubleXpItem;
private ShopItem avatarFrameItem;

@BeforeEach
void setUp() {
user = User.builder().id(USER_ID).coins(1000).streakFreezeCount(0).currentEnergy(10).maxEnergy(25).build();

streakFreezeItem = ShopItem.builder()
.id(ITEM_ID)
.name("Streak Freeze")
.description("Bảo vệ chuỗi ngày học")
.itemType(ShopItem.ItemType.CONSUMABLE)
.effectType(ShopItem.EffectType.STREAK_FREEZE)
.effectValue(1)
.priceCoins(200)
.active(true)
.sortOrder(1)
.build();

doubleXpItem = ShopItem.builder()
.id(2L)
.name("Double XP Boost (30 phút)")
.description("Nhận gấp đôi EXP trong 30 phút")
.itemType(ShopItem.ItemType.POWERUP)
.effectType(ShopItem.EffectType.DOUBLE_XP)
.effectValue(30)
.priceCoins(150)
.active(true)
.sortOrder(3)
.build();

avatarFrameItem = ShopItem.builder()
.id(3L)
.name("Khung avatar: Sakura")
.description("Khung avatar chủ đề hoa anh đào")
.itemType(ShopItem.ItemType.COSMETIC)
.effectType(ShopItem.EffectType.AVATAR_FRAME)
.effectValue(0)
.priceCoins(500)
.active(true)
.sortOrder(10)
.build();
}

@Test
void getShopItems_returnsGroupedByType() {
when(shopItemRepository.findAllByActiveTrueOrderBySortOrderAsc())
.thenReturn(List.of(streakFreezeItem, doubleXpItem, avatarFrameItem));

Map<ShopItem.ItemType, List<ShopItemResponse>> result = shopService.getShopItems();

assertThat(result).hasSize(3);
assertThat(result.get(ShopItem.ItemType.CONSUMABLE)).hasSize(1);
assertThat(result.get(ShopItem.ItemType.POWERUP)).hasSize(1);
assertThat(result.get(ShopItem.ItemType.COSMETIC)).hasSize(1);
}

@Test
void purchaseItem_success_deductsCoinsAndAddsToInventory() {
when(shopItemRepository.findByIdAndActiveTrue(ITEM_ID)).thenReturn(Optional.of(streakFreezeItem));
when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
when(userInventoryRepository.findByUserIdAndItemId(USER_ID, ITEM_ID)).thenReturn(Optional.empty());

UserInventory savedInventory = UserInventory.builder()
.id(INVENTORY_ID)
.userId(USER_ID)
.itemId(ITEM_ID)
.quantity(1)
.equipped(false)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();
when(userInventoryRepository.save(any())).thenReturn(savedInventory);
when(userRepository.save(any())).thenReturn(user);

ShopPurchaseResponse response = shopService.purchaseItem(USER_ID, ITEM_ID);

assertThat(response.getCoinsSpent()).isEqualTo(200);
assertThat(response.getCurrentCoins()).isEqualTo(800);
assertThat(response.getEffectType()).isEqualTo(ShopItem.EffectType.STREAK_FREEZE);

verify(userRepository).save(user);
verify(coinTransactionRepository).save(any(CoinTransaction.class));
verify(userInventoryRepository).save(any(UserInventory.class));
}

@Test
void purchaseItem_insufficientCoins_throwsException() {
user.setCoins(100);
when(shopItemRepository.findByIdAndActiveTrue(ITEM_ID)).thenReturn(Optional.of(streakFreezeItem));
when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

assertThatThrownBy(() -> shopService.purchaseItem(USER_ID, ITEM_ID))
.isInstanceOf(InsufficientCoinsException.class);

verify(userRepository, never()).save(any());
verify(coinTransactionRepository, never()).save(any());
}

@Test
void purchaseItem_itemNotActive_throwsException() {
when(shopItemRepository.findByIdAndActiveTrue(ITEM_ID)).thenReturn(Optional.empty());

assertThatThrownBy(() -> shopService.purchaseItem(USER_ID, ITEM_ID))
.isInstanceOf(ItemNotAvailableException.class);
}

@Test
void purchaseItem_itemNotAvailable_limitedTimeExpired_throwsException() {
streakFreezeItem.setLimitedTime(true);
streakFreezeItem.setAvailableUntil(LocalDateTime.now().minusDays(1));
when(shopItemRepository.findByIdAndActiveTrue(ITEM_ID)).thenReturn(Optional.of(streakFreezeItem));

assertThatThrownBy(() -> shopService.purchaseItem(USER_ID, ITEM_ID))
.isInstanceOf(ItemNotAvailableException.class);
}

@Test
void consumeItem_streakFreeze_incrementsFreezeCount() {
UserInventory inventoryItem = UserInventory.builder()
.id(INVENTORY_ID)
.userId(USER_ID)
.itemId(ITEM_ID)
.quantity(1)
.equipped(false)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();

when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem));
when(shopItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(streakFreezeItem));
when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
when(userRepository.save(any())).thenReturn(user);

ConsumeItemResponse response = shopService.consumeItem(USER_ID, INVENTORY_ID);

assertThat(response.getEffectType()).isEqualTo(ShopItem.EffectType.STREAK_FREEZE);
assertThat(response.getStreakFreezeCount()).isEqualTo(1);
assertThat(response.getEffectDescription()).contains("Streak Freeze");

verify(userInventoryRepository).delete(inventoryItem); // quantity becomes 0
verify(userRepository).save(user);
}

@Test
void consumeItem_energyRefill_refillsEnergy() {
ShopItem energyRefillItem = ShopItem.builder()
.id(4L)
.name("Energy Refill")
.effectType(ShopItem.EffectType.ENERGY_REFILL)
.itemType(ShopItem.ItemType.CONSUMABLE)
.priceCoins(400)
.active(true)
.build();

UserInventory inventoryItem = UserInventory.builder()
.id(INVENTORY_ID)
.userId(USER_ID)
.itemId(4L)
.quantity(1)
.equipped(false)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();

when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem));
when(shopItemRepository.findById(4L)).thenReturn(Optional.of(energyRefillItem));
when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
when(userRepository.save(any())).thenReturn(user);

ConsumeItemResponse response = shopService.consumeItem(USER_ID, INVENTORY_ID);

assertThat(response.getEffectType()).isEqualTo(ShopItem.EffectType.ENERGY_REFILL);
assertThat(response.getCurrentEnergy()).isEqualTo(25);

verify(userRepository).save(user);
}

    @Test
    void consumeItem_doubleXp_setsActiveEffect() {
        UserInventory inventoryItem = UserInventory.builder()
                .id(INVENTORY_ID)
                .userId(USER_ID)
                .itemId(2L)
                .quantity(1)
                .equipped(false)
                .acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
                .build();

        when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem));
        when(shopItemRepository.findById(2L)).thenReturn(Optional.of(doubleXpItem));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        ConsumeItemResponse response = shopService.consumeItem(USER_ID, INVENTORY_ID);

        assertThat(response.getEffectType()).isEqualTo(ShopItem.EffectType.DOUBLE_XP);
        assertThat(response.getEffectDescription()).contains("30 phút");

        verify(userActiveEffectRepository).save(any(UserActiveEffect.class));

// For powerup, quantity is decremented and deleted if reaches 0
// Since quantity was 1, it gets deleted
verify(userInventoryRepository).delete(inventoryItem);
verify(userInventoryRepository, never()).save(any());
}

@Test
void consumeItem_cosmetic_throwsException() {
UserInventory inventoryItem = UserInventory.builder()
.id(INVENTORY_ID)
.userId(USER_ID)
.itemId(3L)
.quantity(1)
.equipped(false)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();

when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem));
when(shopItemRepository.findById(3L)).thenReturn(Optional.of(avatarFrameItem));
when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

assertThatThrownBy(() -> shopService.consumeItem(USER_ID, INVENTORY_ID))
.isInstanceOf(IllegalStateException.class)
.hasMessageContaining("trang trí không thể sử dụng");
}

@Test
void consumeItem_wrongUser_throwsException() {
UserInventory inventoryItem = UserInventory.builder()
.id(INVENTORY_ID)
.userId(999L) // different user
.itemId(ITEM_ID)
.quantity(1)
.equipped(false)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();

when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem));

assertThatThrownBy(() -> shopService.consumeItem(USER_ID, INVENTORY_ID))
.isInstanceOf(InventoryItemNotFoundException.class);
}

@Test
void equipCosmetic_success_togglesEquipped() {
UserInventory inventoryItem = UserInventory.builder()
.id(INVENTORY_ID)
.userId(USER_ID)
.itemId(3L)
.quantity(1)
.equipped(false)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();

when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem));
when(shopItemRepository.findById(3L)).thenReturn(Optional.of(avatarFrameItem));
when(userInventoryRepository.findEquippedCosmeticByUserIdAndEffectType(USER_ID, ShopItem.EffectType.AVATAR_FRAME))
.thenReturn(Collections.emptyList());
when(userInventoryRepository.save(any())).thenReturn(inventoryItem);

InventoryItemResponse response = shopService.equipCosmetic(USER_ID, INVENTORY_ID);

assertThat(response.getEquipped()).isTrue();

// Unequip
when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem));
when(userInventoryRepository.save(any())).thenReturn(inventoryItem);

response = shopService.equipCosmetic(USER_ID, INVENTORY_ID);
assertThat(response.getEquipped()).isFalse();
}

@Test
void equipCosmetic_unequipsOtherSameType() {
UserInventory inventoryItem1 = UserInventory.builder()
.id(INVENTORY_ID)
.userId(USER_ID)
.itemId(3L)
.quantity(1)
.equipped(false)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();

UserInventory inventoryItem2 = UserInventory.builder()
.id(20L)
.userId(USER_ID)
.itemId(4L)
.quantity(1)
.equipped(true)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();

when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem1));
when(shopItemRepository.findById(3L)).thenReturn(Optional.of(avatarFrameItem));
when(userInventoryRepository.findEquippedCosmeticByUserIdAndEffectType(USER_ID, ShopItem.EffectType.AVATAR_FRAME))
.thenReturn(List.of(inventoryItem2));
when(userInventoryRepository.save(any())).thenReturn(inventoryItem1);

InventoryItemResponse response = shopService.equipCosmetic(USER_ID, INVENTORY_ID);

assertThat(response.getEquipped()).isTrue();
verify(userInventoryRepository).save(inventoryItem2); // old one unequipped
assertThat(inventoryItem2.getEquipped()).isFalse();
}

@Test
void equipCosmetic_nonCosmetic_throwsException() {
UserInventory inventoryItem = UserInventory.builder()
.id(INVENTORY_ID)
.userId(USER_ID)
.itemId(ITEM_ID)
.quantity(1)
.equipped(false)
.acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
.build();

when(userInventoryRepository.findById(INVENTORY_ID)).thenReturn(Optional.of(inventoryItem));
when(shopItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(streakFreezeItem));

assertThatThrownBy(() -> shopService.equipCosmetic(USER_ID, INVENTORY_ID))
.isInstanceOf(IllegalStateException.class)
.hasMessageContaining("Chỉ có thể trang bị vật phẩm trang trí");
}

    @Test
    void hasActivePowerup_returnsTrue_whenPowerupActive() {
        when(userActiveEffectRepository.existsByUserIdAndEffectTypeAndExpiresAtAfter(eq(USER_ID), eq(ShopItem.EffectType.DOUBLE_XP), any()))
                .thenReturn(true);

        boolean result = shopService.hasActivePowerup(USER_ID, ShopItem.EffectType.DOUBLE_XP);

        assertThat(result).isTrue();
    }

    @Test
    void hasActivePowerup_returnsFalse_whenNoActivePowerup() {
        when(userActiveEffectRepository.existsByUserIdAndEffectTypeAndExpiresAtAfter(eq(USER_ID), eq(ShopItem.EffectType.DOUBLE_XP), any()))
                .thenReturn(false);

        boolean result = shopService.hasActivePowerup(USER_ID, ShopItem.EffectType.DOUBLE_XP);

        assertThat(result).isFalse();
    }
}