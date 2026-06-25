package com.passaaqui.backend.modules.route.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.cache.CacheService;
import com.passaaqui.backend.modules.route.dto.LocationDTO;
import com.passaaqui.backend.modules.route.dto.RouteDestinationDTO;
import com.passaaqui.backend.modules.route.dto.RouteSessionDTO;
import com.passaaqui.backend.modules.route.dto.StartRouteDTO;
import com.passaaqui.backend.modules.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RouteService {

    private static final String ROUTE_KEY_PREFIX = "route:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(25);

    private final CacheService cacheService;
    private final WebSocketService webSocketService;

    public RouteSessionDTO start(String userId, StartRouteDTO dto) {
        String key = ROUTE_KEY_PREFIX + userId;

        Optional<RouteSessionDTO> existing = cacheService.get(key, RouteSessionDTO.class);
        if (existing.isPresent()) {
            cacheService.setWithTtl(key, existing.get(), SESSION_TTL);
            return existing.get();
        }

        LocationDTO lastLocation = null;
        if (dto.latitude() != null && dto.longitude() != null) {
            lastLocation = new LocationDTO(dto.latitude(), dto.longitude());
        }

        RouteSessionDTO session = new RouteSessionDTO("ACTIVE", null, lastLocation);
        cacheService.setWithTtl(key, session, SESSION_TTL);
        return session;
    }

    public void updateDestination(String userId, RouteDestinationDTO destination) {
        String key = ROUTE_KEY_PREFIX + userId;

        Optional<RouteSessionDTO> existing = cacheService.get(key, RouteSessionDTO.class);
        if (existing.isPresent()) {
            RouteSessionDTO updated = new RouteSessionDTO(existing.get().status(), destination, existing.get().lastLocation());
            cacheService.setWithTtl(key, updated, SESSION_TTL);
        }
    }

    public RouteSessionDTO getCurrentSession(String userId) {
        String key = ROUTE_KEY_PREFIX + userId;
        return cacheService.get(key, RouteSessionDTO.class)
                .orElseThrow(() -> new ResourceNotFoundException("No active route session found"));
    }

    public void stop(String userId) {
        String key = ROUTE_KEY_PREFIX + userId;
        cacheService.delete(key);
        webSocketService.pushToUser(userId, "/queue/route", "route-ended", "Session closed");
    }
}
