package com.passaaqui.backend.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CatalogMetricsDTO(
    @JsonProperty("total_products") long totalProducts,
    @JsonProperty("active_products") long activeProducts,
    @JsonProperty("highlight_products") long highlightProducts
) {}