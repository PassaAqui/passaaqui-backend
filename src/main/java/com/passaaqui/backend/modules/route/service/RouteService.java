package com.passaaqui.backend.modules.route.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.cache.CacheService;
import com.passaaqui.backend.modules.route.dto.LocationDTO;
import com.passaaqui.backend.modules.route.dto.RouteDestinationDTO;
import com.passaaqui.backend.modules.route.dto.RouteSessionDTO;
import com.passaaqui.backend.modules.route.dto.StartRouteDTO;
import com.passaaqui.backend.modules.websocket.WebSocketTopics;
import com.passaaqui.backend.modules.websocket.service.WebSocketService;
import com.passaaqui.backend.modules.websocket.service.WebSocketSessionManager;
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
    private final WebSocketSessionManager sessionManager;

    public RouteSessionDTO start(String userId, StartRouteDTO dto) {
        String key = ROUTE_KEY_PREFIX + userId;

        LocationDTO lastLocation = null;
        if (dto.latitude() != null && dto.longitude() != null) {
            lastLocation = new LocationDTO(dto.latitude(), dto.longitude());
        }

        RouteSessionDTO session = new RouteSessionDTO("ACTIVE", null, lastLocation);

        boolean created = cacheService.setIfAbsent(key, session, SESSION_TTL);
        if (!created) {
            Optional<RouteSessionDTO> existing = cacheService.get(key, RouteSessionDTO.class);
            if (existing.isPresent()) {
                cacheService.setWithTtl(key, existing.get(), SESSION_TTL);
                return existing.get();
            }
        }

        return session;
    }

    public void updateDestination(String userId, RouteDestinationDTO destination) {
        String key = ROUTE_KEY_PREFIX + userId;

        RouteSessionDTO existing = cacheService.get(key, RouteSessionDTO.class)
                .orElseThrow(() -> new InvalidRequestException("No active route session. Start a route first."));

        RouteSessionDTO updated = new RouteSessionDTO(existing.status(), destination, existing.lastLocation());
        cacheService.setIfPresent(key, updated, SESSION_TTL);

        webSocketService.pushToUser(userId, "/queue/route", "destination-updated", destination);
    }

    public RouteSessionDTO getCurrentSession(String userId) {
        String key = ROUTE_KEY_PREFIX + userId;
        return cacheService.get(key, RouteSessionDTO.class)
                .orElseThrow(() -> new ResourceNotFoundException("No active route session found"));
    }

    public void updateLocation(String userId, LocationDTO location) {
        String key = ROUTE_KEY_PREFIX + userId;

        RouteSessionDTO existing = cacheService.get(key, RouteSessionDTO.class)
                .orElseThrow(() -> new InvalidRequestException("No active route session. Start a route first."));

        RouteSessionDTO updated = new RouteSessionDTO(existing.status(), existing.destination(), location);
        cacheService.setIfPresent(key, updated, SESSION_TTL);

        webSocketService.pushToTopic(WebSocketTopics.routeTracking(userId), "location-update", location);
    }

    public void stop(String userId) {
        String key = ROUTE_KEY_PREFIX + userId;
        cacheService.delete(key);
        sessionManager.removeUser(userId);
        webSocketService.pushToUser(userId, "/queue/route", "route-ended", "Session closed");
    }
}
