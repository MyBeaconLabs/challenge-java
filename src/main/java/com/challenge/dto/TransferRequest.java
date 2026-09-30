package com.challenge.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransferRequest(
    @NotNull Long fromWalletId,
    @NotNull Long toWalletId,
    @NotNull BigDecimal amount,
    @Size(max = 255) String note
) {}
