package com.challenge.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotNull Long userId,
    @NotNull List<Item> items
) {

    public record Item(
        @NotNull Long productId,
        @NotNull Integer quantity,
        @NotNull BigDecimal unitPrice
    ) {}
}
