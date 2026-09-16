package com.passaaqui.backend.modules.order.repository;

import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderModel, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderModel o WHERE o.id = :id")
    Optional<OrderModel> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderModel o WHERE o.transactionId = :transactionId")
    Optional<OrderModel> findByTransactionIdForUpdate(@Param("transactionId") String transactionId);

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

    boolean existsByTourist_IdAndProduct_Id(Integer touristId, Integer productId);
}