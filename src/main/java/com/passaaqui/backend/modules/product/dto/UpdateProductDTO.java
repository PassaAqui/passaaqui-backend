package com.passaaqui.backend.modules.product.dto;

public record UpdateProductDTO(
    String name,
    String description,
    Double price,
    Integer maxXp,
    Integer stock,
    Integer shopkeeperId,
    Integer poiId,
    Integer categoryId,
    Boolean active,
    Boolean highlight
) {}
