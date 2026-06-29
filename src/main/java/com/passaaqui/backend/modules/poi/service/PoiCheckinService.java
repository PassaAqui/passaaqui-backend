package com.passaaqui.backend.modules.poi.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.poi.dto.CheckinRequestDTO;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.PoiVisitModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import com.passaaqui.backend.modules.poi.repository.PoiVisitRepository;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import com.passaaqui.backend.modules.user.model.UserModel;
import com.passaaqui.backend.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PoiCheckinService {

    private static final int VISIBILITY_WINDOW_DAYS = 30;

    private final PoiRepository poiRepository;
    private final PoiVisitRepository poiVisitRepository;
    private final UserRepository userRepository;
    private final TouristRepository touristRepository;
    private final XpCalculationService xpCalculationService;

    @Transactional
    public CheckinResponseDTO checkin(Integer poiId, Integer userId, CheckinRequestDTO request) {
        PoiModel poi = poiRepository.findById(poiId)
                .orElseThrow(() -> new ResourceNotFoundException("POI not found"));

        if (poi.getType() != PoiType.TOURIST_POINT) {
            return new CheckinResponseDTO(0, null,
                    new CheckinResponseDTO.RegrasAplicadas(false, false),
                    "POI não é do tipo turístico");
        }

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        double distanciaKm = request.distanciaKm() != null ? request.distanciaKm() : 0.0;

        LocalDateTime since = LocalDateTime.now().minusDays(VISIBILITY_WINDOW_DAYS);
        long visitasRecentes = poiVisitRepository
                .countByPoiIdAndVisitedAtAfterAndUserIdNot(poiId, since, userId);

        LocalDateTime ultimoCheckin = poiVisitRepository
                .findFirstByPoiIdAndUserIdOrderByVisitedAtDesc(poiId, userId)
                .map(PoiVisitModel::getVisitedAt)
                .orElse(null);

        CheckinResponseDTO response = xpCalculationService.calculate(
                userId, poiId, "turistico",
                distanciaKm, (int) visitasRecentes, ultimoCheckin
        );

        PoiVisitModel visit = new PoiVisitModel();
        visit.setPoi(poi);
        visit.setUser(user);
        visit.setDistanceKm(distanciaKm);
        visit.setXpEarned(response.xpConcedido());
        poiVisitRepository.save(visit);

        if (response.xpConcedido() > 0) {
            TouristModel tourist = touristRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));
            tourist.setCurrentXP(tourist.getCurrentXP() + response.xpConcedido());
            touristRepository.save(tourist);
        }

        return response;
    }
}
