package com.passaaqui.backend.modules.poi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePoiDTO(
    @NotBlank
    String name,

    String description,

    @Min(0)
    Integer xpReward,

    Double latitude,

    Double longitude,

    Double minLatitude,

    Double maxLatitude,

    Double minLongitude,

    Double maxLongitude,

    @NotNull
    Integer cityId
) {}
