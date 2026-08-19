package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.ConsumeItemResponse;
import com.example.nihongo_app.dto.response.InventoryItemResponse;
import com.example.nihongo_app.dto.response.ShopItemResponse;
import com.example.nihongo_app.dto.response.ShopPurchaseResponse;
import com.example.nihongo_app.entity.CoinTransaction;
import com.example.nihongo_app.entity.ShopItem;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserActiveEffect;
import com.example.nihongo_app.entity.UserInventory;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemRepository shopItemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final UserActiveEffectRepository userActiveEffectRepository;

    @Transactional(readOnly = true)
    public Map<ShopItem.ItemType, List<ShopItemResponse>> getShopItems() {
        List<ShopItem> items = shopItemRepository.findAllByActiveTrueOrderBySortOrderAsc();
        return items.stream()
                .collect(Collectors.groupingBy(
                        ShopItem::getItemType,
                        Collectors.mapping(this::toShopItemResponse, Collectors.toList())
                ));
    }

    @Transactional
    public ShopPurchaseResponse purchaseItem(Long userId, Long itemId) {
        ShopItem shopItem = shopItemRepository.findByIdAndActiveTrue(itemId)
                .orElseThrow(() -> new ItemNotAvailableException("Mặt hàng không tồn tại hoặc đã ngừng bán."));

        if (!shopItem.isAvailable()) {
            throw new ItemNotAvailableException("Mặt hàng này hiện không khả dụng.");
        }

        User user = getUser(userId);
        int currentCoins = user.getCoins() == null ? 0 : user.getCoins();
        int price = shopItem.getPriceCoins();

        if (currentCoins < price) {
            throw new InsufficientCoinsException(
                    "Không đủ coins. Cần " + price + ", hiện có " + currentCoins);
        }

        int newCoins = currentCoins - price;
        user.setCoins(newCoins);
        userRepository.save(user);

        coinTransactionRepository.save(CoinTransaction.builder()
                .userId(userId)
                .amount(-price)
                .transactionType(CoinTransaction.TransactionType.SHOP_PURCHASE)
                .referenceId(itemId)
                .build());

        UserInventory existing = userInventoryRepository.findByUserIdAndItemId(userId, itemId).orElse(null);
        UserInventory inventoryItem;
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            inventoryItem = userInventoryRepository.save(existing);
        } else {
            inventoryItem = userInventoryRepository.save(UserInventory.builder()
                    .userId(userId)
                    .itemId(itemId)
                    .quantity(1)
                    .equipped(false)
                    .acquiredFrom(UserInventory.AcquiredFrom.SHOP_BUY)
                    .build());
        }

        return ShopPurchaseResponse.builder()
                .inventoryId(inventoryItem.getId())
                .itemName(shopItem.getName())
                .effectType(shopItem.getEffectType())
                .coinsSpent(price)
                .currentCoins(newCoins)
                .message("Mua thành công: " + shopItem.getName())
                .build();
    }

    @Transactional
    public ConsumeItemResponse consumeItem(Long userId, Long inventoryId) {
        UserInventory inventoryItem = userInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryItemNotFoundException("Không tìm thấy vật phẩm trong túi đồ."));

        if (!inventoryItem.getUserId().equals(userId)) {
            throw new InventoryItemNotFoundException("Vật phẩm này không thuộc về bạn.");
        }

        if (inventoryItem.getQuantity() <= 0) {
            throw new InventoryItemNotFoundException("Vật phẩm đã hết số lượng.");
        }

        ShopItem shopItem = shopItemRepository.findById(inventoryItem.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin vật phẩm."));

        User user = getUser(userId);

        String effectDescription = "";
        Integer newStreakFreezeCount = user.getStreakFreezeCount();
        Integer newCurrentEnergy = user.getCurrentEnergy();
        Integer newCoins = user.getCoins();

        switch (shopItem.getEffectType()) {
            case STREAK_FREEZE -> {
                newStreakFreezeCount = (user.getStreakFreezeCount() == null ? 0 : user.getStreakFreezeCount()) + 1;
                user.setStreakFreezeCount(newStreakFreezeCount);
                effectDescription = "Đã kích hoạt Streak Freeze. Bạn có " + newStreakFreezeCount + " lần bảo vệ chuỗi ngày học.";
            }
            case ENERGY_REFILL -> {
                int maxEnergy = user.getMaxEnergy() == null ? 25 : user.getMaxEnergy();
                newCurrentEnergy = maxEnergy;
                user.setCurrentEnergy(newCurrentEnergy);
                effectDescription = "Đã hồi đầy năng lượng (" + maxEnergy + "/" + maxEnergy + ").";
            }
            case DOUBLE_XP, DOUBLE_COIN, TIMER_BOOST -> {
                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(shopItem.getEffectValue());
                UserActiveEffect effect = UserActiveEffect.builder()
                        .userId(userId)
                        .effectType(shopItem.getEffectType())
                        .expiresAt(expiresAt)
                        .build();
                userActiveEffectRepository.save(effect);
                effectDescription = "Đã kích hoạt " + shopItem.getName() + " trong " + shopItem.getEffectValue() + " phút.";
            }
            case AVATAR_FRAME, BADGE, THEME -> {
                throw new IllegalStateException("Vật phẩm trang trí không thể sử dụng, hãy trang bị (equip) thay thế.");
            }
        }

        inventoryItem.setQuantity(inventoryItem.getQuantity() - 1);
        if (inventoryItem.getQuantity() == 0) {
            userInventoryRepository.delete(inventoryItem);
        } else {
            userInventoryRepository.save(inventoryItem);
        }

        userRepository.save(user);

        coinTransactionRepository.save(CoinTransaction.builder()
                .userId(userId)
                .amount(0)
                .transactionType(CoinTransaction.TransactionType.ITEM_CONSUME)
                .referenceId(inventoryItem.getItemId())
                .build());

        return ConsumeItemResponse.builder()
                .itemName(shopItem.getName())
                .effectType(shopItem.getEffectType())
                .effectDescription(effectDescription)
                .currentCoins(newCoins)
                .currentEnergy(newCurrentEnergy)
                .streakFreezeCount(newStreakFreezeCount)
                .message("Sử dụng thành công: " + shopItem.getName())
                .build();
    }

    @Transactional
    public InventoryItemResponse equipCosmetic(Long userId, Long inventoryId) {
        UserInventory inventoryItem = userInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryItemNotFoundException("Không tìm thấy vật phẩm trong túi đồ."));

        if (!inventoryItem.getUserId().equals(userId)) {
            throw new InventoryItemNotFoundException("Vật phẩm này không thuộc về bạn.");
        }

        ShopItem shopItem = shopItemRepository.findById(inventoryItem.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin vật phẩm."));

        if (shopItem.getItemType() != ShopItem.ItemType.COSMETIC) {
            throw new IllegalStateException("Chỉ có thể trang bị vật phẩm trang trí (cosmetic).");
        }

        boolean newEquipped = !Boolean.TRUE.equals(inventoryItem.getEquipped());

        if (newEquipped) {
            List<UserInventory> sameTypeItems = userInventoryRepository
                    .findEquippedCosmeticByUserIdAndEffectType(userId, shopItem.getEffectType());
            sameTypeItems.forEach(item -> {
                item.setEquipped(false);
                userInventoryRepository.save(item);
            });
        }

        inventoryItem.setEquipped(newEquipped);
        userInventoryRepository.save(inventoryItem);

        return toInventoryItemResponse(inventoryItem, shopItem);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getUserInventory(Long userId) {
        List<UserInventory> items = userInventoryRepository.findAllByUserId(userId);

        List<Long> itemIds = items.stream()
                .map(UserInventory::getItemId)
                .distinct()
                .toList();

        Map<Long, ShopItem> itemMap;
        if (itemIds.isEmpty()) {
            itemMap = Map.of();
        } else {
            List<ShopItem> shopItems = shopItemRepository.findByIdIn(itemIds);
            itemMap = shopItems.stream()
                    .collect(Collectors.toMap(ShopItem::getId, si -> si));
        }

        return items.stream()
                .map(ui -> toInventoryItemResponse(ui, itemMap.get(ui.getItemId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserActiveEffect> getActivePowerups(Long userId) {
        return userActiveEffectRepository.findByUserIdAndExpiresAtAfter(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public boolean hasActivePowerup(Long userId, ShopItem.EffectType effectType) {
        return userActiveEffectRepository.existsByUserIdAndEffectTypeAndExpiresAtAfter(userId, effectType, LocalDateTime.now());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với id=" + userId));
    }

    private ShopItemResponse toShopItemResponse(ShopItem item) {
        return ShopItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .itemType(item.getItemType())
                .effectType(item.getEffectType())
                .effectValue(item.getEffectValue())
                .priceCoins(item.getPriceCoins())
                .priceGems(item.getPriceGems())
                .iconUrl(item.getIconUrl())
                .sortOrder(item.getSortOrder())
                .limitedTime(item.getLimitedTime())
                .availableFrom(item.getAvailableFrom())
                .availableUntil(item.getAvailableUntil())
                .build();
    }

    private InventoryItemResponse toInventoryItemResponse(UserInventory inv, ShopItem item) {
        if (item == null) {
            return null;
        }
        return InventoryItemResponse.builder()
                .inventoryId(inv.getId())
                .itemId(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .itemType(item.getItemType())
                .effectType(item.getEffectType())
                .effectValue(item.getEffectValue())
                .quantity(inv.getQuantity())
                .equipped(inv.getEquipped())
                .active(false)
                .iconUrl(item.getIconUrl())
                .expiresAt(null)
                .acquiredAt(inv.getAcquiredAt())
                .build();
    }
}
