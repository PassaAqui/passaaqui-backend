package com.passaaqui.backend.modules.poi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CheckinResponseDTO(
        @JsonProperty("xp_concedido") int xpGranted,
        @JsonProperty("calculo") Calculation calculation,
        @JsonProperty("regras_aplicadas") AppliedRules appliedRules,
        @JsonProperty("motivo_bloqueio") String blockReason
) {
    public record Calculation(
            @JsonProperty("distancia_km") double distanceKm,
            @JsonProperty("fator_deslocamento") double displacementFactor,
            @JsonProperty("visitas_recentes") int recentVisits,
            @JsonProperty("fator_invisibilidade") double invisibilityFactor,
            @JsonProperty("xp_bruto") double rawXp,
            @JsonProperty("xp_final") int finalXp
    ) {}

    public record AppliedRules(
            @JsonProperty("anti_farming_ativo") boolean antiFarmingActive,
            @JsonProperty("gps_invalido") boolean invalidGps
    ) {}
}
