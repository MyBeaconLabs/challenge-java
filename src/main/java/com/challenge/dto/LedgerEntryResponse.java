package com.challenge.dto;

import com.challenge.entity.LedgerEntry;
import com.challenge.util.Money;
import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryResponse(Long id, Long transactionId, BigDecimal amount, long amountMinor, Instant createdAt) {

    public static LedgerEntryResponse from(LedgerEntry entry, String currency) {
        return new LedgerEntryResponse(
            entry.getId(),
            entry.getTransaction().getId(),
            Money.toMajorUnits(entry.getAmountMinor(), currency),
            entry.getAmountMinor(),
            entry.getCreatedAt());
    }
}
