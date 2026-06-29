package com.passaaqui.backend.modules.route.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.cache.CacheService;
import com.passaaqui.backend.modules.poi.dto.CheckinRequestDTO;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import com.passaaqui.backend.modules.poi.service.PoiCheckinService;
import com.passaaqui.backend.modules.poi.service.GeoUtils;
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
    private static final double ARRIVAL_THRESHOLD_KM = 0.1;

    private final CacheService cacheService;
    private final WebSocketService webSocketService;
    private final WebSocketSessionManager sessionManager;
    private final PoiCheckinService poiCheckinService;
    private final PoiRepository poiRepository;

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

        checkArrival(userId, existing, location);
    }

    public void stop(String userId) {
        String key = ROUTE_KEY_PREFIX + userId;
        cacheService.delete(key);
        sessionManager.removeUser(userId);
        webSocketService.pushToUser(userId, "/queue/route", "route-ended", "Session closed");
    }

    private void checkArrival(String userId, RouteSessionDTO session, LocationDTO currentLocation) {
        RouteDestinationDTO destination = session.destination();
        if (destination == null || destination.poiId() == null) return;
        if (currentLocation.latitude() == null || currentLocation.longitude() == null) return;

        PoiModel poi = poiRepository.findById(destination.poiId()).orElse(null);
        if (poi == null || poi.getLatitude() == null || poi.getLongitude() == null) return;

        boolean arrived;
        if (poi.getMinLatitude() != null && poi.getMaxLatitude() != null
                && poi.getMinLongitude() != null && poi.getMaxLongitude() != null) {
            arrived = currentLocation.latitude() >= poi.getMinLatitude()
                    && currentLocation.latitude() <= poi.getMaxLatitude()
                    && currentLocation.longitude() >= poi.getMinLongitude()
                    && currentLocation.longitude() <= poi.getMaxLongitude();
        } else {
            double distance = GeoUtils.haversineKm(
                    currentLocation.latitude(), currentLocation.longitude(),
                    poi.getLatitude(), poi.getLongitude());
            arrived = distance <= ARRIVAL_THRESHOLD_KM;
        }

        if (!arrived) return;

        Integer userIdInt = Integer.parseInt(userId);

        LocationDTO startLocation = session.lastLocation();
        double distanceKm = ARRIVAL_THRESHOLD_KM;
        if (startLocation != null && startLocation.latitude() != null && startLocation.longitude() != null) {
            distanceKm = GeoUtils.haversineKm(
                    startLocation.latitude(), startLocation.longitude(),
                    poi.getLatitude(), poi.getLongitude());
        }
        if (distanceKm < ARRIVAL_THRESHOLD_KM) {
            distanceKm = ARRIVAL_THRESHOLD_KM;
        }

        CheckinResponseDTO response = poiCheckinService.checkin(
                destination.poiId(), userIdInt, new CheckinRequestDTO(distanceKm));

        webSocketService.pushToUser(userId, "/queue/poi", "checkin-result", response);
    }
}
