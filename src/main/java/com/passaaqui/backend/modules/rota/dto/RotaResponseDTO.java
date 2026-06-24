package com.passaaqui.backend.modules.rota.dto;

import java.util.UUID;

public record RotaResponseDTO(
        UUID id,
        Double startLatitude,
        Double startLongitude,
        Double endLatitude,
        Double endLongitude,
        String mode,
        Object route
) {
}
