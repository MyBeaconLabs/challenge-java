package com.challenge.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Amount in major units of the wallet's currency, e.g. 25.50 for USD. */
public record DepositRequest(@NotNull @Positive BigDecimal amount) {}
