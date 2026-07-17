package com.passaaqui.backend.modules.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateProductRatingDTO(
    @NotNull
    @Min(0)
    @Max(5)
    Integer rating
) {}