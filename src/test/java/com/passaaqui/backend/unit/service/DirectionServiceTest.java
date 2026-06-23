package com.passaaqui.backend.unit.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.integration.openrouteservice.OpenRouteServiceClient;
import com.passaaqui.backend.modules.direction.dto.DirectionRequestDTO;
import com.passaaqui.backend.modules.direction.model.enums.DirectionMode;
import com.passaaqui.backend.modules.direction.service.DirectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectionServiceTest {

    @Mock
    private OpenRouteServiceClient openRouteServiceClient;

    @InjectMocks
    private DirectionService service;

    @Test
    void getDirections_shouldReturnResult_whenValidMode() {
        var dto = new DirectionRequestDTO("driving-car", -46.6576, -23.5874, -46.6333, -23.5505);
        var expected = Map.of("routes", "some route data");

        when(openRouteServiceClient.getDirections(any(DirectionRequestDTO.class), any(DirectionMode.class)))
                .thenReturn(expected);

        var result = service.getDirections(dto);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(openRouteServiceClient).getDirections(eq(dto), eq(DirectionMode.DRIVING_CAR));
    }

    @Test
    void getDirections_shouldThrow_whenInvalidMode() {
        var dto = new DirectionRequestDTO("invalid-mode", 0, 0, 0, 0);

        assertThrows(InvalidRequestException.class, () -> service.getDirections(dto));
        verify(openRouteServiceClient, never()).getDirections(any(), any());
    }
}
