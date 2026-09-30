package com.challenge.dto;

import com.challenge.entity.Transfer;
import java.time.Instant;

public record TransferResponse(
    Long id,
    Long fromWalletId,
    Long toWalletId,
    double amount,
    String status,
    String error,
    Instant createdAt
) {

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
            transfer.getId(),
            transfer.getFromWalletId(),
            transfer.getToWalletId(),
            transfer.getAmountMinor() / 100.0,
            transfer.getStatus(),
            null,
            transfer.getCreatedAt());
    }

    public static TransferResponse failed(String error) {
        return new TransferResponse(null, null, null, 0, "FAILED", error, Instant.now());
    }
}
