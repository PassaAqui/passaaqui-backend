package com.passaaqui.backend.modules.order.dto;

import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusDTO(
    @NotNull
    OrderStatus status
) {}