
package com.orderflow.order.application;

import com.orderflow.catalog.domain.Product;
import com.orderflow.catalog.infrastructure.ProductRepository;
import com.orderflow.order.domain.Order;
import com.orderflow.order.domain.OrderStatus;
import com.orderflow.order.infrastructure.OrderRepository;
import com.orderflow.order.presentation.dto.CreateOrderRequest;
import com.orderflow.order.presentation.dto.OrderItemRequest;
import com.orderflow.shared.exception.ResourceConflictException;
import com.orderflow.shared.exception.ResourceNotFoundException;
import com.orderflow.user.domain.Role;
import com.orderflow.user.domain.User;
import com.orderflow.user.infrastructure.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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


    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User(
                UUID.randomUUID(),
                "Henrique",
                "henrique@email.com",
                "password-hash",
                Role.CUSTOMER,
                null,
                null
        );

        product = new Product(
                UUID.randomUUID(),
                null,
                "Mechanical Keyboard",
                "Mechanical keyboard with RGB",
                "KEYBOARD-001",
                new BigDecimal("50.00"),
                10,
                true,
                null,
                null
        );
    }


    @Test
    void shouldCreateOrderSuccessfully() {

        // Arrange
        when(userRepository.findByEmail("henrique@email.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(productRepository.reserveStock(product.getId(), 2))
                .thenReturn(1);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 2))
        );

        // Act
        var response = orderService.createOrder(
                "henrique@email.com",
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(new BigDecimal("100.00"), response.totalAmount());
        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());

        verify(productRepository).reserveStock(product.getId(), 2);
        verify(orderRepository).save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        // Arrange
        when(userRepository.findByEmail("henrique@email.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(productRepository.reserveStock(product.getId(), 2))
                .thenReturn(0);

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 2))
        );

        // Act & Assert
        assertThrows(
                ResourceConflictException.class,
                () -> orderService.createOrder("henrique@email.com", request)
        );

        verify(productRepository).reserveStock(product.getId(), 2);

        verify(orderRepository, never()).save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        // Arrange
        UUID productId = UUID.randomUUID();

        when(userRepository.findByEmail("henrique@email.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(productId, 1))
        );

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder("henrique@email.com", request)
        );

        verify(productRepository).findById(productId);

        verify(productRepository, never())
                .reserveStock(any(UUID.class), anyInt());

        verify(orderRepository, never()).save(any(Order.class));
    }


    @Test
    void shouldNotAllowUserToAccessAnotherUsersOrder() {

        // Arrange
        UUID orderId = UUID.randomUUID();

        when(userRepository.findByEmail("henrique@email.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(orderId, user.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.findById("henrique@email.com", orderId)
        );

        verify(orderRepository)
                .findByIdAndUserId(orderId, user.getId());
    }


    @Test
    void shouldReturnOnlyOrdersFromAuthenticatedUser() {

        // Arrange
        Order firstOrder = new Order(
                UUID.randomUUID(),
                user,
                OrderStatus.PENDING,
                new BigDecimal("100.00"),
                new ArrayList<>(),
                null,
                null
        );

        Order secondOrder = new Order(
                UUID.randomUUID(),
                user,
                OrderStatus.PENDING,
                new BigDecimal("200.00"),
                new ArrayList<>(),
                null,
                null
        );

        when(userRepository.findByEmail("henrique@email.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(firstOrder, secondOrder));

        // Act
        var response = orderService.findAllByUser("henrique@email.com");

        // Assert
        assertEquals(2, response.size());

        verify(orderRepository).findAllByUserId(user.getId());
    }

}
