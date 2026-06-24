package com.passaaqui.backend.modules.rota.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.integration.openrouteservice.OpenRouteServiceClient;
import com.passaaqui.backend.modules.direction.dto.DirectionRequestDTO;
import com.passaaqui.backend.modules.rota.dto.RotaResponseDTO;
import com.passaaqui.backend.modules.rota.dto.StartRotaDTO;
import com.passaaqui.backend.modules.rota.model.RotaModel;
import com.passaaqui.backend.modules.rota.repository.RotaRepository;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RotaService {

    private final RotaRepository rotaRepository;
    private final TouristRepository touristRepository;
    private final OpenRouteServiceClient openRouteServiceClient;

    @Transactional
    public RotaResponseDTO start(StartRotaDTO dto, Integer touristId) {
        TouristModel tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new InvalidRequestException("Tourist not found"));

        RotaModel rota = new RotaModel();
        rota.setTourist(tourist);
        rota.setStartLatitude(dto.startLatitude());
        rota.setStartLongitude(dto.startLongitude());
        rota.setEndLatitude(dto.endLatitude());
        rota.setEndLongitude(dto.endLongitude());
        rota.setMode(dto.mode());
        rota.setPoiId(dto.poiId());
        rotaRepository.save(rota);

        DirectionRequestDTO directionRequest = new DirectionRequestDTO(
                dto.mode(),
                dto.startLongitude(),
                dto.startLatitude(),
                dto.endLongitude(),
                dto.endLatitude()
        );

        Object route;
        try {
            route = openRouteServiceClient.getDirections(directionRequest,
                    com.passaaqui.backend.modules.direction.model.enums.DirectionMode.fromMode(dto.mode()));
        } catch (Exception e) {
            throw new InvalidRequestException("Failed to calculate route: " + e.getMessage());
        }

        return new RotaResponseDTO(
                rota.getId(),
                rota.getStartLatitude(),
                rota.getStartLongitude(),
                rota.getEndLatitude(),
                rota.getEndLongitude(),
                rota.getMode(),
                route
        );
    }
}
