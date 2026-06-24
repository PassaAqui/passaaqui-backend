package com.passaaqui.backend.modules.rota.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StartRotaDTO(
        @NotNull
        Double startLatitude,

        @NotNull
        Double startLongitude,

        @NotNull
        Double endLatitude,

        @NotNull
        Double endLongitude,

        @NotBlank
        String mode,

        Integer poiId
) {
}
