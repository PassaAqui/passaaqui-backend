package com.passaaqui.backend.modules.order.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequestDTO(

    @NotNull
    Integer productId,

    Integer xpToUse
) {}
