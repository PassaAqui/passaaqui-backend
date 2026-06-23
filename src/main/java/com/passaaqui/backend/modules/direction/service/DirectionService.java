package com.passaaqui.backend.modules.direction.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.integration.openrouteservice.OpenRouteServiceClient;
import com.passaaqui.backend.modules.direction.dto.DirectionRequestDTO;
import com.passaaqui.backend.modules.direction.model.enums.DirectionMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectionService {

    private final OpenRouteServiceClient openRouteServiceClient;

    public Object getDirections(DirectionRequestDTO dto) {
        DirectionMode mode;
        try {
            mode = DirectionMode.fromMode(dto.mode());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }

        return openRouteServiceClient.getDirections(dto, mode);
    }

}
