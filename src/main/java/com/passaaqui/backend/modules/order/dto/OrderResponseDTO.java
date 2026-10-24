package com.passaaqui.backend.modules.order.dto;

import com.passaaqui.backend.modules.order.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponseDTO(
    UUID id,
    Integer productId,
    String productName,
    Integer shopkeeperId,
    String shopkeeperName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalAmount,
    OrderStatus status,
    String transactionId,
    LocalDateTime createdAt,
    String pix,
    String qrCodeBase64,
    LocalDateTime pixExpiresAt,
    String pickupCode,
    Integer xpUsed,
    BigDecimal cashDiscount
) {
    public OrderResponseDTO(
            UUID id,
            Integer productId,
            String productName,
            Integer shopkeeperId,
            String shopkeeperName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            OrderStatus status,
            String transactionId,
            LocalDateTime createdAt,
            String pix,
            String qrCodeBase64,
            LocalDateTime pixExpiresAt,
            String pickupCode
    ) {
        this(id, productId, productName, shopkeeperId, shopkeeperName, quantity, unitPrice, totalAmount, status, transactionId, createdAt, pix, qrCodeBase64, pixExpiresAt, pickupCode, null, null);
    }
}

