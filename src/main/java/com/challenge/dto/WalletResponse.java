package com.challenge.dto;

import com.challenge.entity.Wallet;
import com.challenge.util.Money;
import java.math.BigDecimal;
import java.time.Instant;

public record WalletResponse(
    Long id,
    Long userId,
    String currency,
    BigDecimal balance,
    long balanceMinor,
    Instant createdAt
) {

    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
            wallet.getId(),
            wallet.getUserId(),
            wallet.getCurrency(),
            Money.toMajorUnits(wallet.getBalanceMinor(), wallet.getCurrency()),
            wallet.getBalanceMinor(),
            wallet.getCreatedAt());
    }
}
