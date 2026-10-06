package com.passaaqui.backend.modules.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductDTO(
    @NotBlank
    String name,

    String description,

    @NotNull
    @jakarta.validation.constraints.DecimalMin(value = "5.0", message = "Price must be at least 5.00")
    Double price,

    Integer maxXp,

    @Min(0)
    Integer stock,

    @NotNull
    Integer shopkeeperId,

    @NotNull
    Integer poiId,

    @NotNull
    Integer categoryId,

    Boolean active,

    Boolean highlight
) {}
