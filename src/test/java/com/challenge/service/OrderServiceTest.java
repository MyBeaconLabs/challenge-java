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

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product keyboard;
    private Product mouse;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john.doe@example.com");
        testUser.setId(1L);

        keyboard = new Product();
        keyboard.setId(10L);
        keyboard.setName("Keyboard");
        keyboard.setCategory("Electronics");
        keyboard.setPrice(new BigDecimal("49.99"));
        keyboard.setStockQuantity(5);

        mouse = new Product();
        mouse.setId(20L);
        mouse.setName("Mouse");
        mouse.setCategory("Electronics");
        mouse.setPrice(new BigDecimal("19.50"));
        mouse.setStockQuantity(10);
    }

    @Test
    void createOrder_ShouldCalculateTotalAndReserveStock() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(keyboard));
        when(productRepository.findById(20L)).thenReturn(Optional.of(mouse));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(
            new CreateOrderRequest.Item(10L, 2, new BigDecimal("49.99")),
            new CreateOrderRequest.Item(20L, 1, new BigDecimal("19.50"))));

        // When
        Order order = orderService.createOrder(request);

        // Then
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(2, order.getItems().size());
        assertEquals(0, new BigDecimal("119.48").compareTo(order.getTotalAmount()));
        assertEquals(3, keyboard.getStockQuantity());
        assertEquals(9, mouse.getStockQuantity());
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void createOrder_WhenInsufficientStock_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(keyboard));

        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(
            new CreateOrderRequest.Item(10L, 6, new BigDecimal("49.99"))));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenOrderExists_ShouldDeleteOrder() {
        // Given
        when(orderRepository.existsById(5L)).thenReturn(true);

        // When
        orderService.cancelOrder(5L);

        // Then
        verify(orderRepository, times(1)).deleteById(5L);
    }

    @Test
    void cancelOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        // Given
        when(orderRepository.existsById(99L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(99L));
        verify(orderRepository, never()).deleteById(any());
    }
}
