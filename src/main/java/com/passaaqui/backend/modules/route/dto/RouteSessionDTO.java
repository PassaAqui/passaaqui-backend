package com.passaaqui.backend.modules.route.dto;

public record RouteSessionDTO(
        String status,
        String destination,
        Object lastLocation
) {
}
