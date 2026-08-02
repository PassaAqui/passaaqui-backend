package com.passaaqui.backend.modules.poi.dto;

import jakarta.validation.constraints.Min;

public record CheckinRequestDTO(
        @Min(0)
        Double distanceKm
) {
}
