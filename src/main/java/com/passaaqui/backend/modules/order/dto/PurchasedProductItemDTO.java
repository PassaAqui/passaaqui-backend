package com.passaaqui.backend.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.order.model.enums.RedemptionStatus;

import java.time.LocalDate;

public record PurchasedProductItemDTO(
    @JsonProperty("product_id")
    Integer productId,

    @JsonProperty("order_id")
    String orderId,

    @JsonProperty("product_name")
    String productName,

    @JsonProperty("image_url")
    String imageUrl,

    RedemptionStatus status,

    @JsonProperty("expiration_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate expirationDate,

    @JsonProperty("redemption_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate redemptionDate
) {}
