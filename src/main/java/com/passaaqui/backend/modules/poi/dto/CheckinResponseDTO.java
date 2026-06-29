package com.passaaqui.backend.modules.poi.dto;

public record CheckinResponseDTO(
        int xpConcedido,
        Calculo calculo,
        RegrasAplicadas regrasAplicadas,
        String motivoBloqueio
) {
    public record Calculo(
            double distanciaKm,
            double fatorDeslocamento,
            int visitasRecentes,
            double fatorInvisibilidade,
            double xpBruto,
            int xpFinal
    ) {}

    public record RegrasAplicadas(
            boolean antiFarmingAtivo,
            boolean gpsInvalido
    ) {}
}
