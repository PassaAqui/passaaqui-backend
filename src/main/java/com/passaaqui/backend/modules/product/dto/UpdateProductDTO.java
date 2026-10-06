package com.passaaqui.backend.modules.product.dto;

public record UpdateProductDTO(
    String name,
    String description,
    @jakarta.validation.constraints.DecimalMin(value = "5.0", message = "Price must be at least 5.00")
    Double price,
    Integer maxXp,
    Integer stock,
    Integer shopkeeperId,
    Integer poiId,
    Integer categoryId,
    Boolean active,
    Boolean highlight
) {}
