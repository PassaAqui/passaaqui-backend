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

    List<OrderModel> findByShopkeeper_IdAndStatus(Integer shopkeeperId, OrderStatus status);

    List<OrderModel> findByShopkeeper_IdOrderByCreatedAtDesc(Integer shopkeeperId);

    List<OrderModel> findByShopkeeper_IdAndStatusOrderByCreatedAtDesc(Integer shopkeeperId, OrderStatus status);

    List<OrderModel> findTop5ByShopkeeper_IdOrderByCreatedAtDesc(Integer shopkeeperId);

    List<OrderModel> findByTourist_IdOrderByCreatedAtDesc(Integer touristId);

    Optional<OrderModel> findTopByTourist_IdAndStatusOrderByCreatedAtDesc(Integer touristId, OrderStatus status);

    boolean existsByTourist_IdAndStatusNotIn(Integer touristId, List<OrderStatus> statuses);

    long countByShopkeeper_IdAndStatusAndCreatedAtBetween(Integer shopkeeperId, OrderStatus status, LocalDateTime start, LocalDateTime end);

    long countByShopkeeper_IdAndCreatedAtBetween(Integer shopkeeperId, LocalDateTime start, LocalDateTime end);
}