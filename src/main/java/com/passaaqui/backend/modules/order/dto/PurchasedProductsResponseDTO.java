package com.passaaqui.backend.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PurchasedProductsResponseDTO(
    @JsonProperty("nao_resgatados")
    List<PurchasedProductItemDTO> unredeemed,

    @JsonProperty("resgatados")
    List<PurchasedProductItemDTO> redeemed
) {}
