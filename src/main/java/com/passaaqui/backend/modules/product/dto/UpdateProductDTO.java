package com.passaaqui.backend.modules.product.dto;

public record UpdateProductDTO(
    String name,
    String description,
    Double price,
    Integer maxXp,
    Integer shopkeeperId,
    Integer categoryId
) {}
