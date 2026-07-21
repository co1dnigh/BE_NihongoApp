package com.nihongoapp.wallet.controller;

import com.nihongoapp.common.util.RsUtil;
import com.nihongoapp.wallet.dto.WalletBalanceResponse;
import com.nihongoapp.wallet.model.WalletTransactionReason;
import com.nihongoapp.wallet.service.WalletService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        WalletBalanceResponse balance = walletService.getBalance(userId);
        return ResponseEntity.ok(RsUtil.ok(balance));
    }

    @PostMapping("/grant")
    public ResponseEntity<?> grantCurrency(
            Principal principal,
            @RequestBody GrantRequest request) {
        Long userId = Long.valueOf(principal.getName());
        WalletTransactionReason reason = WalletTransactionReason.valueOf(request.reason().toUpperCase());
        WalletBalanceResponse result = walletService.addCurrency(
                userId, request.amount(), reason, request.idempotencyKey());
        return ResponseEntity.ok(RsUtil.ok(result));
    }

    public record GrantRequest(
            @NotNull @Min(1) Long amount,
            @NotBlank String reason,
            String idempotencyKey
    ) {}
}
