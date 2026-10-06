package com.passaaqui.backend.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ProductRatingResponseDTO(
    Integer id,

    @JsonProperty("product_id")
    Integer productId,

    @JsonProperty("product_name")
    String productName,

    @JsonProperty("order_id")
    String orderId,

    Integer rating,

    String comment,

    List<String> photos,

    String video,

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {}
