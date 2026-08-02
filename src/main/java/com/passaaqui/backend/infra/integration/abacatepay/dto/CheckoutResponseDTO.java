package com.passaaqui.backend.infra.integration.abacatepay.dto;

import java.util.Map;

public record CheckoutResponseDTO(
        String id,
        Integer amount,
        String status,
        Boolean devMode,
        String brCode,
        String brCodeBase64,
        Integer platformFee,
        String receiptUrl,
        String createdAt,
        String updatedAt,
        String expiresAt,
        Map<String, Object> metadata
) {}