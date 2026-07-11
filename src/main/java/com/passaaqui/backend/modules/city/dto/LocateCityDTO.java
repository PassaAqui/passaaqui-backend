package com.passaaqui.backend.modules.city.dto;

import jakarta.validation.constraints.NotNull;

public record LocateCityDTO(
        @NotNull
        Double latitude,

        @NotNull
        Double longitude
) {}
