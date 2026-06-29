package com.passaaqui.backend.modules.poi.service;

import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO.Calculation;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO.AppliedRules;
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
            Integer userId,
            Integer poiId,
            String poiType,
            double distanceKm,
            int recentVisits,
            LocalDateTime lastUserCheckin
    ) {
        boolean antiFarmingActive = false;
        boolean invalidGps = false;
        String blockReason = null;

        if (!"turistico".equals(poiType)) {
            return new CheckinResponseDTO(0, null, new AppliedRules(false, false), "POI não é do tipo turístico");
        }

        if (lastUserCheckin != null
                && ChronoUnit.DAYS.between(lastUserCheckin, LocalDateTime.now()) < COOLDOWN_DAYS) {
            antiFarmingActive = true;
            blockReason = "Cooldown ativo (30 dias).";
        }

        if (distanceKm < MIN_DISTANCE_KM) {
            invalidGps = true;
            blockReason = "Deslocamento insuficiente detectado.";
        }

        if (antiFarmingActive || invalidGps) {
            return new CheckinResponseDTO(0, null, new AppliedRules(antiFarmingActive, invalidGps), blockReason);
        }

        double displacementFactor = distanceKm * DISPLACEMENT_FACTOR;
        double invisibilityFactor = (double) INVISIBILITY_BASE / (recentVisits + 1);
        double rawXp = displacementFactor * invisibilityFactor;
        int finalXp = (int) Math.round(rawXp);

        if (finalXp < 0) finalXp = 0;

        Calculation calculation = new Calculation(distanceKm, displacementFactor, recentVisits, invisibilityFactor, rawXp, finalXp);

        return new CheckinResponseDTO(finalXp, calculation, new AppliedRules(false, false), null);
    }
}
