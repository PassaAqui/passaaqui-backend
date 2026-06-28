package com.passaaqui.backend.modules.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductDTO(
    @NotBlank
    String name,

    String description,

    @Min(0)
    Double price,

    Integer maxXp,

    @Min(0)
    Integer stock,

    @NotNull
    Integer shopkeeperId,

    @NotNull
    Integer poiId,

    @NotNull
    Integer categoryId
) {}
