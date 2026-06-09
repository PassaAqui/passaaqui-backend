package com.passaaqui.backend.modules.poi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreatePoiRatingDTO(
    @NotNull
    @Min(0)
    @Max(5)
    Integer rating
) {}
