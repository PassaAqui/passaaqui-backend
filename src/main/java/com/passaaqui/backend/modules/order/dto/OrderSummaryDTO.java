package com.passaaqui.backend.modules.order.dto;

import java.math.BigDecimal;

public record OrderSummaryDTO(
    Integer productId,
    BigDecimal unitPrice,
    Integer maxXp,
    Integer availableXp,
    Integer xpToUse,
    BigDecimal xpDiscount,
    BigDecimal totalAmount
) {}
