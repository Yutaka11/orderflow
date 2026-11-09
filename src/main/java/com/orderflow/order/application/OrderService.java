package com.orderflow.order.application;

import com.orderflow.catalog.domain.Product;
import com.orderflow.catalog.infrastructure.ProductRepository;
import com.orderflow.order.domain.Order;
import com.orderflow.order.domain.OrderItem;
import com.orderflow.order.domain.OrderStatus;
import com.orderflow.order.infrastructure.OrderRepository;
import com.orderflow.order.presentation.dto.CreateOrderRequest;
import com.orderflow.order.presentation.dto.OrderItemRequest;
import com.orderflow.order.presentation.dto.OrderItemResponse;
import com.orderflow.order.presentation.dto.OrderResponse;
import com.orderflow.shared.exception.ResourceConflictException;
import com.orderflow.shared.exception.ResourceNotFoundException;
import com.orderflow.user.domain.User;
import com.orderflow.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(
            String userEmail,
            CreateOrderRequest request
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
        Order order = new Order(
                null,
                user,
                OrderStatus.PENDING,
                BigDecimal.ZERO,
                new ArrayList<>(),
                null,
                null
        );
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<java.util.UUID> processedProducts = new HashSet<>();

        for (OrderItemRequest itemRequest : request.items()) {

            if (!processedProducts.add(itemRequest.productId())) {
                throw new ResourceConflictException(
                        "Duplicate product in order"
                );
            }
            Product product = productRepository
                    .findById(itemRequest.productId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found: " + itemRequest.productId()
                            )
                    );
            int reserved = productRepository.reserveStock(
                    product.getId(),
                    itemRequest.quantity()
            );
            if (reserved == 0) {
                throw new ResourceConflictException(
                        "Insufficient stock for product: " + product.getName()
                );
            }
            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.quantity())
            );
            OrderItem orderItem = new OrderItem(
                    null,
                    order,
                    product,
                    product.getName(),
                    unitPrice,
                    itemRequest.quantity(),
                    subtotal
            );
            order.addItem(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAllByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        return orderRepository.findAllByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(String userEmail, UUID orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
        Order order = orderRepository.findByIdAndUserId(
                orderId,
                user.getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Order not found")
        );
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt()
        );
    }
}