package com.passaaqui.backend.unit.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.cache.CacheService;
import com.passaaqui.backend.modules.route.dto.LocationDTO;
import com.passaaqui.backend.modules.route.dto.RouteDestinationDTO;
import com.passaaqui.backend.modules.route.dto.RouteSessionDTO;
import com.passaaqui.backend.modules.route.dto.StartRouteDTO;
import com.passaaqui.backend.modules.route.service.RouteService;
import com.passaaqui.backend.modules.websocket.service.WebSocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private RouteService routeService;

    @Captor
    private ArgumentCaptor<RouteSessionDTO> sessionCaptor;

    private static final String USER_ID = "user-1";

    @Test
    void start_shouldCreateNewSession_whenNoExistingSession() {
        when(cacheService.get("route:" + USER_ID, RouteSessionDTO.class)).thenReturn(Optional.empty());

        var dto = new StartRouteDTO(-23.5505, -46.6333);
        var result = routeService.start(USER_ID, dto);

        assertNotNull(result);
        assertEquals("ACTIVE", result.status());
        assertNull(result.destination());
        assertNotNull(result.lastLocation());
        assertEquals(-23.5505, result.lastLocation().latitude());
        assertEquals(-46.6333, result.lastLocation().longitude());

        verify(cacheService).setWithTtl(eq("route:" + USER_ID), sessionCaptor.capture(), any());
        var saved = sessionCaptor.getValue();
        assertEquals("ACTIVE", saved.status());
    }

    @Test
    void start_shouldRenewTtlAndReturnExisting_whenSessionExists() {
        var existing = new RouteSessionDTO("ACTIVE", null, null);
        when(cacheService.get("route:" + USER_ID, RouteSessionDTO.class)).thenReturn(Optional.of(existing));

        var result = routeService.start(USER_ID, new StartRouteDTO(null, null));

        assertSame(existing, result);
        verify(cacheService).setWithTtl(eq("route:" + USER_ID), eq(existing), any());
    }

    @Test
    void start_shouldCreateSessionWithoutLocation_whenDtoHasNullCoordinates() {
        when(cacheService.get("route:" + USER_ID, RouteSessionDTO.class)).thenReturn(Optional.empty());

        var result = routeService.start(USER_ID, new StartRouteDTO(null, null));

        assertNotNull(result);
        assertEquals("ACTIVE", result.status());
        assertNull(result.lastLocation());
    }

    @Test
    void updateDestination_shouldUpdateSession_whenSessionExists() {
        var existing = new RouteSessionDTO("ACTIVE", null, null);
        when(cacheService.get("route:" + USER_ID, RouteSessionDTO.class)).thenReturn(Optional.of(existing));

        var destination = new RouteDestinationDTO(-23.5505, -46.6333, -23.5610, -46.6560, "driving-car");
        routeService.updateDestination(USER_ID, destination);

        verify(cacheService).setWithTtl(eq("route:" + USER_ID), sessionCaptor.capture(), any());
        var updated = sessionCaptor.getValue();
        assertEquals("ACTIVE", updated.status());
        assertNotNull(updated.destination());
        assertEquals("driving-car", updated.destination().mode());
    }

    @Test
    void updateDestination_shouldDoNothing_whenNoSession() {
        when(cacheService.get("route:" + USER_ID, RouteSessionDTO.class)).thenReturn(Optional.empty());

        routeService.updateDestination(USER_ID, new RouteDestinationDTO(0.0, 0.0, 0.0, 0.0, "driving-car"));

        verify(cacheService, never()).setWithTtl(any(), any(), any());
    }

    @Test
    void getCurrentSession_shouldReturnSession_whenExists() {
        var expected = new RouteSessionDTO("ACTIVE", null, null);
        when(cacheService.get("route:" + USER_ID, RouteSessionDTO.class)).thenReturn(Optional.of(expected));

        var result = routeService.getCurrentSession(USER_ID);

        assertSame(expected, result);
    }

    @Test
    void getCurrentSession_shouldThrow_whenNoSession() {
        when(cacheService.get("route:" + USER_ID, RouteSessionDTO.class)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routeService.getCurrentSession(USER_ID));
    }

    @Test
    void stop_shouldDeleteSessionAndNotifySocket() {
        routeService.stop(USER_ID);

        verify(cacheService).delete("route:" + USER_ID);
        verify(webSocketService).pushToUser(eq(USER_ID), eq("/queue/route"), eq("route-ended"), eq("Session closed"));
    }
}
