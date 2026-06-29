package com.passaaqui.backend.modules.route.dto;

public record StartRouteDTO(
        Double latitude,
        Double longitude,
        Integer poiId
) {
}
