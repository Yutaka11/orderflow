package com.orderflow.order.infrastructure;

import com.orderflow.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByUserId(UUID userId);

    Optional<Order> findByIdAndUserId(UUID orderId, UUID userId);


}