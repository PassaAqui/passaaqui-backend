package com.passaaqui.backend.modules.user.dto;

import com.passaaqui.backend.modules.user.model.enums.MapViewPreference;
import jakarta.validation.constraints.NotNull;

public record UpdateMapViewDTO(
    @NotNull(message = "Map view preference is required")
    MapViewPreference mapView
) {}
