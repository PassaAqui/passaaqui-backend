package com.passaaqui.backend.modules.order.repository;

import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderModel, UUID> {

    Optional<OrderModel> findByTransactionId(String transactionId);

    List<OrderModel> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime createdAt);
}