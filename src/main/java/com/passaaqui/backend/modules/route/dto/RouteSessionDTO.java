package com.passaaqui.backend.modules.route.dto;

public record RouteSessionDTO(
        String status,
        RouteDestinationDTO destination,
        LocationDTO lastLocation
) {
}
