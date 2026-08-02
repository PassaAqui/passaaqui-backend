package com.passaaqui.backend.modules.route.dto;

public record RouteDestinationDTO(
        Double startLatitude,
        Double startLongitude,
        Double stopLatitude,
        Double stopLongitude,
        String mode,
        Integer poiId
) {
}
