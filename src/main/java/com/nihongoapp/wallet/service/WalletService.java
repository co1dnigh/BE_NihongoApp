package com.nihongoapp.wallet.service;

import com.nihongoapp.common.exception.BusinessException;
import com.nihongoapp.wallet.dto.WalletBalanceResponse;
import com.nihongoapp.wallet.model.UserWallet;
import com.nihongoapp.wallet.model.WalletTransaction;
import com.nihongoapp.wallet.model.WalletTransactionReason;
import com.nihongoapp.wallet.repository.UserWalletRepository;
import com.nihongoapp.wallet.repository.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private static final String IDEM_KEY_PREFIX = "idem:wallet:";
    private static final int MAX_RETRIES = 3;

    private final UserWalletRepository walletRepo;
    private final WalletTransactionRepository txRepo;
    private final RedisTemplate<String, String> redisStringTemplate;

    public WalletService(
            UserWalletRepository walletRepo,
            WalletTransactionRepository txRepo,
            @Qualifier("redisStringTemplate") RedisTemplate<String, String> redisStringTemplate) {
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
        this.redisStringTemplate = redisStringTemplate;
    }

    @Transactional
    public WalletBalanceResponse getBalance(Long userId) {
        UserWallet wallet = walletRepo.findByUserId(userId)
                .orElseGet(() -> createWallet(userId));
        return new WalletBalanceResponse(wallet.getGemBalance());
    }

    private UserWallet createWallet(Long userId) {
        UserWallet wallet = new UserWallet();
        wallet.setUserId(userId);
        return walletRepo.save(wallet);
    }

    @Transactional
    public WalletBalanceResponse addCurrency(Long userId,
                                             WalletTransactionReason reason,
                                             String idempotencyKey) {
        Long amount = switch (reason) {
            case LESSON_REWARD -> -1L; // negative = caller must pass absolute value
            default -> throw new IllegalArgumentException("Use specific method");
        };
        return addCurrency(userId, amount, reason, idempotencyKey);
    }

    @Transactional
    public WalletBalanceResponse addCurrency(Long userId, Long amount,
                                             WalletTransactionReason reason,
                                             String idempotencyKey) {
        if (amount == null || amount == 0) {
            throw new IllegalArgumentException("amount must not be zero");
        }
        String redisKey = IDEM_KEY_PREFIX + idempotencyKey;
        Boolean set = redisStringTemplate.opsForValue()
                .setIfAbsent(redisKey, "processing", 600, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(set)) {
            log.info("Duplicate idempotency_key={} for user={}", idempotencyKey, userId);
            return getBalance(userId);
        }

        try {
            WalletBalanceResponse result = doAddCurrencyWithRetry(userId, amount, reason, idempotencyKey);
            return result;
        } catch (DataIntegrityViolationException e) {
            log.info("Idempotency DB caught duplicate key={} for user={}", idempotencyKey, userId);
            return getBalance(userId);
        } finally {
            redisStringTemplate.delete(redisKey);
        }
    }

    private WalletBalanceResponse doAddCurrencyWithRetry(Long userId, Long amount,
                                                          WalletTransactionReason reason,
                                                          String idempotencyKey) {
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                return doAddCurrency(userId, amount, reason, idempotencyKey);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                if (attempts >= MAX_RETRIES) {
                    throw new BusinessException("WALLET_ERROR", "Transaction failed after retries", e);
                }
                try { Thread.sleep(50L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }

    @Transactional
    protected WalletBalanceResponse doAddCurrency(Long userId, Long amount,
                                                  WalletTransactionReason reason,
                                                  String idempotencyKey) {
        UserWallet wallet = walletRepo.findByUserIdWithLock(userId)
                .orElseGet(() -> {
                    UserWallet w = new UserWallet();
                    w.setUserId(userId);
                    return walletRepo.save(w);
                });

        long after = wallet.getGemBalance() + amount;
        if (after < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "Not enough GEM");
        }

        wallet.setGemBalance(after);
        wallet = walletRepo.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setCurrencyType(com.nihongoapp.wallet.model.CurrencyType.GEM);
        tx.setReason(reason);
        tx.setIdempotencyKey(idempotencyKey);
        tx.setBalanceAfter(after);
        txRepo.save(tx);

        return new WalletBalanceResponse(wallet.getGemBalance());
    }
}
