package com.nihongoapp.shop.service;

import com.nihongoapp.common.exception.BusinessException;
import com.nihongoapp.shop.model.ShopItem;
import com.nihongoapp.shop.repository.ShopItemRepository;
import com.nihongoapp.user.model.User;
import com.nihongoapp.user.repository.UserRepository;
import com.nihongoapp.wallet.model.WalletTransactionReason;
import com.nihongoapp.wallet.repository.UserInventoryRepository;
import com.nihongoapp.wallet.service.WalletService;
import com.nihongoapp.wallet.dto.WalletBalanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class ShopService {

    private static final Logger log = LoggerFactory.getLogger(ShopService.class);
    private static final String IDEM_KEY_PREFIX = "idem:shop:";

    private final ShopItemRepository shopItemRepo;
    private final WalletService walletService;
    // walletRepo not used directly — balance via WalletService
    private final UserRepository userRepo;
    private final UserInventoryRepository inventoryRepo;
    private final RedisTemplate<String, String> redisStringTemplate;

    public ShopService(
            ShopItemRepository shopItemRepo,
            WalletService walletService,
            UserRepository userRepo,
            UserInventoryRepository inventoryRepo,
            @Qualifier("redisStringTemplate") RedisTemplate<String, String> redisStringTemplate) {
        this.shopItemRepo = shopItemRepo;
        this.walletService = walletService;
        this.userRepo = userRepo;
        this.inventoryRepo = inventoryRepo;
        this.redisStringTemplate = redisStringTemplate;
    }

    @Transactional
    public ShopPurchaseResult purchase(Long userId, Long shopItemId, String idempotencyKey) {
        String redisKey = IDEM_KEY_PREFIX + idempotencyKey;
        Boolean set = redisStringTemplate.opsForValue()
                .setIfAbsent(redisKey, "processing", 600, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(set)) {
            throw new BusinessException("CONFLICT", "Request already being processed");
        }

        try {
            return doPurchase(userId, shopItemId);
        } finally {
            redisStringTemplate.delete(redisKey);
        }
    }

    private ShopPurchaseResult doPurchase(Long userId, Long shopItemId) {
        ShopItem item = shopItemRepo.findByIdAndIsActiveTrue(shopItemId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Shop item not found or inactive"));

        int price = item.getPriceGemstone();

        walletService.addCurrency(userId, (long) -price, WalletTransactionReason.SHOP_PURCHASE, "shop:" + userId + ":" + shopItemId);

        if (item.getEffectType() == ShopItem.EffectType.HEART_REFILL) {
            userRepo.addHearts(userId, item.getEffectValue());
        } else if (item.getEffectType() == ShopItem.EffectType.STREAK_FREEZE) {
            inventoryRepo.insertOrAddQuantity(userId, "STREAK_FREEZE", null, item.getEffectValue());
        }

        User user = userRepo.findById(userId).orElseThrow();
        WalletBalanceResponse balance = walletService.getBalance(userId);

        log.info("Shop purchase: userId={}, item={}, price={}, hearts={}", userId, item.getItemName(), price, user.getHearts());

        return new ShopPurchaseResult(
                item.getItemName(),
                item.getEffectType().name(),
                item.getEffectValue(),
                balance.gem(),
                user.getHearts()
        );
    }

    public record ShopPurchaseResult(
            String itemName,
            String effectType,
            int effectValue,
            Long gemBalance,
            Integer heartsRemaining
    ) {}
}
