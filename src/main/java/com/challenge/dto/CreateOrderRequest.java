package com.challenge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotNull Long userId,
    @NotNull @Valid List<Item> items
) {

    public record Item(
        @NotNull Long productId,
        @NotNull Integer quantity,
        @NotNull BigDecimal unitPrice
    ) {}
}
