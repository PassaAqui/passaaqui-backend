package com.passaaqui.backend.modules.direction.dto;

import jakarta.validation.constraints.NotBlank;

public record DirectionRequestDTO(
        @NotBlank
        String mode,
        double startLongitude,
        double startLatitude,
        double endLongitude,
        double endLatitude
) {}
