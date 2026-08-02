package com.passaaqui.backend.modules.city.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCityDTO(
        @NotBlank
        String name,

        @NotBlank
        String description,

        @NotBlank
        String state,

        @NotBlank
        String ibgeCode,

        @NotBlank
        String region,

        @NotBlank
        String microRegion,

        @NotBlank
        String mesoRegion,

        @NotBlank
        String stateName,

        @NotNull
        Long regionCode,

        Double minLatitude,

        Double maxLatitude,

        Double minLongitude,

        Double maxLongitude
) {}