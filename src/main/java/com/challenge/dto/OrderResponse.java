package com.challenge.dto;

import com.challenge.entity.Order;
import java.time.LocalDateTime;

public record OrderResponse(
    Long id,
    Long userId,
    String status,
    double totalAmount,
    int itemCount,
    LocalDateTime orderDate
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getUser().getId(),
            order.getStatus().name(),
            order.getTotalAmount().doubleValue(),
            order.getItems().size(),
            order.getOrderDate());
    }
}
