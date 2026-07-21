package com.nihongoapp.wallet.repository;

import com.nihongoapp.wallet.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
}
