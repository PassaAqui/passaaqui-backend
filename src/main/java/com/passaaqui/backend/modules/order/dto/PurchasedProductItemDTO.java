package com.passaaqui.backend.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.order.model.enums.RedemptionStatus;

import java.time.LocalDate;

public record PurchasedProductItemDTO(
    @JsonProperty("id_pedido")
    String orderId,

    @JsonProperty("nome_produto")
    String productName,

    @JsonProperty("imagem_url")
    String imageUrl,

    RedemptionStatus status,

    @JsonProperty("data_validade")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate expirationDate,

    @JsonProperty("data_resgate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate redemptionDate
) {}
