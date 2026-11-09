package com.orderflow.order.presentation;

import com.orderflow.order.application.OrderService;
import com.orderflow.order.presentation.dto.CreateOrderRequest;
import com.orderflow.order.presentation.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {
        OrderResponse response = orderService.createOrder(
                authentication.getName(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                orderService.findAllByUser(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                orderService.findById(authentication.getName(), id)
        );
    }
}