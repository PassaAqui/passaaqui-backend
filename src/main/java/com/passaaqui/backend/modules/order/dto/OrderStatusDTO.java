package com.passaaqui.backend.modules.order.dto;

import com.passaaqui.backend.modules.order.model.enums.OrderStatus;

import java.util.UUID;

public record OrderStatusDTO(
    UUID id,
    OrderStatus status,
    String pickupCode
) {}
