package com.passaaqui.backend.modules.direction.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.integration.openrouteservice.OpenRouteServiceClient;
import com.passaaqui.backend.modules.direction.dto.DirectionRequestDTO;
import com.passaaqui.backend.modules.direction.model.enums.DirectionMode;
import com.passaaqui.backend.modules.route.dto.RouteDestinationDTO;
import com.passaaqui.backend.modules.route.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectionService {

    private final OpenRouteServiceClient openRouteServiceClient;
    private final RouteService routeService;

    public Object getDirections(DirectionRequestDTO dto, String userId) {
        DirectionMode mode;
        try {
            mode = DirectionMode.fromMode(dto.mode());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }

        RouteDestinationDTO destination = new RouteDestinationDTO(
                dto.startLatitude(), dto.startLongitude(),
                dto.endLatitude(), dto.endLongitude(),
                dto.mode(),
                dto.poiId()
        );
        routeService.updateDestination(userId, destination);

        return openRouteServiceClient.getDirections(dto, mode);
    }

}
