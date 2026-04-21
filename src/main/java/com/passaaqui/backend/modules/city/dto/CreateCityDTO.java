package com.passaaqui.backend.modules.city.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCityDTO (
        @NotBlank
        String ibgeCode,

        @NotBlank
        String description
) {}
