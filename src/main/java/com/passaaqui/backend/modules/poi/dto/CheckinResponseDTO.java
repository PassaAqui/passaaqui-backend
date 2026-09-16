package com.passaaqui.backend.modules.poi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CheckinResponseDTO(
        @JsonProperty("xp_granted") int xpGranted,
        @JsonProperty("calculation") Calculation calculation,
        @JsonProperty("applied_rules") AppliedRules appliedRules,
        @JsonProperty("block_reason") String blockReason
) {
    public record Calculation(
            @JsonProperty("distance_km") double distanceKm,
            @JsonProperty("displacement_factor") double displacementFactor,
            @JsonProperty("recent_visits") int recentVisits,
            @JsonProperty("invisibility_factor") double invisibilityFactor,
            @JsonProperty("raw_xp") double rawXp,
            @JsonProperty("final_xp") int finalXp
    ) {}

    public record AppliedRules(
            @JsonProperty("anti_farming_active") boolean antiFarmingActive,
            @JsonProperty("invalid_gps") boolean invalidGps
    ) {}
}
