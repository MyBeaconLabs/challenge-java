package com.challenge.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(Long userId, List<Item> items) {

    public record Item(Long productId, Integer quantity, BigDecimal unitPrice) {}
}
