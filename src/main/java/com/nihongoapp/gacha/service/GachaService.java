package com.nihongoapp.gacha.service;

import com.nihongoapp.common.exception.BusinessException;
import com.nihongoapp.gacha.model.GachaBanner;
import com.nihongoapp.gacha.model.GachaItem;
import com.nihongoapp.gacha.repository.GachaBannerRepository;
import com.nihongoapp.gacha.repository.GachaItemRepository;
import com.nihongoapp.user.model.User;
import com.nihongoapp.user.repository.UserRepository;
import com.nihongoapp.wallet.model.UserGachaPity;
import com.nihongoapp.wallet.model.WalletTransactionReason;
import com.nihongoapp.wallet.repository.UserGachaPityRepository;
import com.nihongoapp.wallet.repository.UserInventoryRepository;
import com.nihongoapp.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GachaService {

  private static final Logger log = LoggerFactory.getLogger(GachaService.class);
  private static final String IDEM_KEY_PREFIX = "idem:gacha:";
  private static final SecureRandom RNG = new SecureRandom();
  private static final int SSR_PITY_LIMIT = 90;

  private final GachaBannerRepository bannerRepo;
  private final GachaItemRepository itemRepo;
  private final WalletService walletService;
  private final UserGachaPityRepository pityRepo;
  private final UserRepository userRepo;
  private final UserInventoryRepository inventoryRepo;
  private final RedisTemplate<String, String> redisStringTemplate;

  public GachaService(
      GachaBannerRepository bannerRepo,
      GachaItemRepository itemRepo,
      WalletService walletService,
      UserGachaPityRepository pityRepo,
      UserRepository userRepo,
      UserInventoryRepository inventoryRepo,
      @Qualifier("redisStringTemplate") RedisTemplate<String, String> redisStringTemplate) {
    this.bannerRepo = bannerRepo;
    this.itemRepo = itemRepo;
    this.walletService = walletService;
    this.pityRepo = pityRepo;
    this.userRepo = userRepo;
    this.inventoryRepo = inventoryRepo;
    this.redisStringTemplate = redisStringTemplate;
  }

  @Transactional(readOnly = true)
  public GachaBanner getBanner(Long bannerId) {
    return bannerRepo.findByIdAndIsActiveTrue(bannerId)
        .orElseThrow(() -> new BusinessException("NOT_FOUND", "Banner not found or inactive"));
  }

  @Transactional
  public GachaSpinResult spin(Long userId, Long bannerId, String idempotencyKey) {
    String redisKey = IDEM_KEY_PREFIX + idempotencyKey;
    Boolean set = redisStringTemplate.opsForValue()
        .setIfAbsent(redisKey, "processing", 600, java.util.concurrent.TimeUnit.SECONDS);
    if (Boolean.FALSE.equals(set)) {
      throw new BusinessException("CONFLICT", "Request already being processed");
    }

    try {
      return doSpin(userId, bannerId, idempotencyKey);
    } finally {
      redisStringTemplate.delete(redisKey);
    }
  }

  private GachaSpinResult doSpin(Long userId, Long bannerId, String idempotencyKey) {
    GachaBanner banner = bannerRepo.findByIdAndIsActiveTrue(bannerId)
        .orElseThrow(() -> new BusinessException("NOT_FOUND", "Banner not found or inactive"));

    User user = userRepo.findById(userId)
        .orElseThrow(() -> new BusinessException("NOT_FOUND", "User not found"));

    int cost = banner.getCostAmount();
    walletService.addCurrency(userId, (long) -cost, WalletTransactionReason.GACHA_SPIN, idempotencyKey);

    GachaSpinResult result = pickItem(bannerId);

    inventoryRepo.insertOrAddQuantity(userId, result.effectType(), result.itemId(), 1);

    UserGachaPity pity = pityRepo
        .findWithPessimisticLockByUserIdAndBannerId(userId, bannerId)
        .orElseGet(() -> {
          UserGachaPity p = new UserGachaPity(user, bannerId);
          return pityRepo.save(p);
        });

    int newPity = pity.getSpinCountSinceLastSsr();

    if (GachaItem.Rarity.SSR.name().equals(result.rarity())) {
      pity.setSpinCountSinceLastSsr(0);
    } else {
      newPity++;
      pity.setSpinCountSinceLastSsr(newPity);
    }

    log.info("Gacha spin result: userId={}, bannerId={}, item={}, rarity={}, pityCount={}/{}",
        userId, bannerId, result.itemName(), result.rarity(), newPity, SSR_PITY_LIMIT);

    pityRepo.save(pity);

    return result;
  }

  private GachaSpinResult pickItem(Long bannerId) {
    List<GachaItem> items = itemRepo.findByBannerId(bannerId);
    if (items.isEmpty()) {
      throw new BusinessException("NOT_FOUND", "No items configured for banner");
    }

    Map<GachaItem.Rarity, List<GachaItem>> byRarity = items.stream()
        .collect(Collectors.groupingBy(GachaItem::getRarity));

    int totalWeight = items.stream().mapToInt(GachaItem::getDropWeight).sum();
    int roll = RNG.nextInt(totalWeight);

    List<GachaItem> pool = resolvePool(roll, byRarity);
    GachaItem picked = pool.get(RNG.nextInt(pool.size()));
    return new GachaSpinResult(
        picked.getItemName(),
        picked.getRarity().name(),
        picked.getEffectType(),
        picked.getEffectValue(),
        picked.getId()
    );
  }

  private List<GachaItem> resolvePool(int roll, Map<GachaItem.Rarity, List<GachaItem>> byRarity) {
    int ssrWeight = byRarity.getOrDefault(GachaItem.Rarity.SSR, List.of()).stream().mapToInt(GachaItem::getDropWeight).sum();
    int srWeight = byRarity.getOrDefault(GachaItem.Rarity.SR, List.of()).stream().mapToInt(GachaItem::getDropWeight).sum();

    if (roll < ssrWeight) {
      return byRarity.getOrDefault(GachaItem.Rarity.SSR, List.of());
    }
    if (roll < ssrWeight + srWeight) {
      return byRarity.getOrDefault(GachaItem.Rarity.SR, List.of());
    }
    return byRarity.getOrDefault(GachaItem.Rarity.R, List.of());
  }

  public record GachaSpinResult(
      String itemName,
      String rarity,
      String effectType,
      int effectValue,
      Long itemId
  ) {}
}
