package com.passaaqui.backend.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ShopkeeperOrderDTO(
    UUID id,
    @JsonProperty("customer_name") String customerName,
    @JsonProperty("created_at") LocalDateTime createdAt,
    OrderStatus status,
    String code,
    BigDecimal total,
    List<OrderItemDTO> items
) {
    public static ShopkeeperOrderDTO from(OrderModel order) {
        return new ShopkeeperOrderDTO(
            order.getId(),
            order.getTourist().getName(),
            order.getCreatedAt(),
            order.getStatus(),
            order.getCode(),
            order.getTotalAmount(),
            List.of(new OrderItemDTO(order.getProduct().getName(), order.getQuantity()))
        );
    }
}