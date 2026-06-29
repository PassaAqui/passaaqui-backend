package com.passaaqui.backend.modules.poi.service;

import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO.Calculo;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO.RegrasAplicadas;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class XpCalculationService {

    private static final double MIN_DISTANCE_KM = 0.1;
    private static final double DISPLACEMENT_FACTOR = 2.5;
    private static final int INVISIBILITY_BASE = 100;
    private static final int COOLDOWN_DAYS = 30;

    public CheckinResponseDTO calculate(
            Integer usuarioId,
            Integer poiId,
            String poiTipo,
            double distanciaKm,
            int visitasRecentes,
            LocalDateTime ultimoCheckinUsuario
    ) {
        boolean antiFarmingAtivo = false;
        boolean gpsInvalido = false;
        String motivoBloqueio = null;

        if (!"turistico".equals(poiTipo)) {
            return new CheckinResponseDTO(0, null, new RegrasAplicadas(false, false), "POI não é do tipo turístico");
        }

        if (ultimoCheckinUsuario != null
                && ChronoUnit.DAYS.between(ultimoCheckinUsuario, LocalDateTime.now()) < COOLDOWN_DAYS) {
            antiFarmingAtivo = true;
            motivoBloqueio = "Cooldown ativo (30 dias).";
        }

        if (distanciaKm < MIN_DISTANCE_KM) {
            gpsInvalido = true;
            motivoBloqueio = "Deslocamento insuficiente detectado.";
        }

        if (antiFarmingAtivo || gpsInvalido) {
            return new CheckinResponseDTO(0, null, new RegrasAplicadas(antiFarmingAtivo, gpsInvalido), motivoBloqueio);
        }

        double fatorDeslocamento = distanciaKm * DISPLACEMENT_FACTOR;
        double fatorInvisibilidade = (double) INVISIBILITY_BASE / (visitasRecentes + 1);
        double xpBruto = fatorDeslocamento * fatorInvisibilidade;
        int xpFinal = (int) Math.round(xpBruto);

        if (xpFinal < 0) xpFinal = 0;

        Calculo calculo = new Calculo(distanciaKm, fatorDeslocamento, visitasRecentes, fatorInvisibilidade, xpBruto, xpFinal);

        return new CheckinResponseDTO(xpFinal, calculo, new RegrasAplicadas(false, false), null);
    }
}
