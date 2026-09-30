package com.challenge.service;

import com.challenge.dto.CreateOrderRequest;
import com.challenge.entity.*;
import com.challenge.repository.OrderRepository;
import com.challenge.repository.ProductRepository;
import com.challenge.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Order createOrder(CreateOrderRequest request) {
        User user = userRepository.findById(request.userId()).get();
        log.info("Creating order for user {}", user.getEmail());

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        for (CreateOrderRequest.Item itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId()).get();
            reserveStock(product, itemRequest.quantity());
            order.getItems().add(new OrderItem(order, product, itemRequest.quantity(), itemRequest.unitPrice()));
        }

        double total = order.getItems().stream()
            .mapToDouble(item -> item.getSubtotal().doubleValue())
            .sum();
        order.setTotalAmount(BigDecimal.valueOf(total));
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    @Transactional
    public void reserveStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product " + product.getId());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    public Optional<Order> getOrder(Long id) {
        return orderRepository.findById(id);
    }

    public void cancelOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}
