package com.challenge.service;

import com.challenge.dto.CreateOrderRequest;
import com.challenge.entity.Order;
import com.challenge.entity.OrderStatus;
import com.challenge.entity.Product;
import com.challenge.entity.User;
import com.challenge.repository.OrderRepository;
import com.challenge.repository.ProductRepository;
import com.challenge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private Product keyboard;
    private Product mouse;

    @BeforeEach
    void setUp() {
        User user = new User("John Doe", "john.doe@example.com");
        user.setId(1L);
        keyboard = product(10L, "49.99", 5);
        mouse = product(20L, "19.50", 10);

        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        lenient().when(productRepository.findById(10L)).thenReturn(Optional.of(keyboard));
        lenient().when(productRepository.findById(20L)).thenReturn(Optional.of(mouse));
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createOrder_calculatesTotalAndReservesStock() {
        Order order = orderService.createOrder(new CreateOrderRequest(1L, List.of(
            new CreateOrderRequest.Item(10L, 2, new BigDecimal("49.99")),
            new CreateOrderRequest.Item(20L, 1, new BigDecimal("19.50")))));

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(0, new BigDecimal("119.48").compareTo(order.getTotalAmount()));
        assertEquals(3, keyboard.getStockQuantity());
        assertEquals(9, mouse.getStockQuantity());
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void createOrder_insufficientStock_throws() {
        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(
            new CreateOrderRequest.Item(10L, 6, new BigDecimal("49.99"))));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_deletesOrder() {
        when(orderRepository.existsById(5L)).thenReturn(true);

        orderService.cancelOrder(5L);

        verify(orderRepository).deleteById(5L);
    }

    private static Product product(Long id, String price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        product.setCategory("Electronics");
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stock);
        return product;
    }
}
