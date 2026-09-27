package com.passaaqui.backend.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PurchasedProductsResponseDTO(
    @JsonProperty("unredeemed")
    List<PurchasedProductItemDTO> unredeemed,

    @JsonProperty("redeemed")
    List<PurchasedProductItemDTO> redeemed
) {}
