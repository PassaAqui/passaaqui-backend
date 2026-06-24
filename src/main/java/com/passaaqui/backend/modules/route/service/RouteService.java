package com.passaaqui.backend.modules.route.service;

import com.passaaqui.backend.infra.integration.cache.CacheService;
import com.passaaqui.backend.modules.route.dto.RouteSessionDTO;
import com.passaaqui.backend.modules.route.dto.StartRouteDTO;
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

    public RouteSessionDTO start(String userId, StartRouteDTO dto) {
        String key = ROUTE_KEY_PREFIX + userId;

        Optional<RouteSessionDTO> existing = cacheService.get(key, RouteSessionDTO.class);
        if (existing.isPresent()) {
            cacheService.setWithTtl(key, existing.get(), SESSION_TTL);
            return existing.get();
        }

        RouteSessionDTO session = new RouteSessionDTO("ACTIVE", dto.destination(), null);
        cacheService.setWithTtl(key, session, SESSION_TTL);
        return session;
    }
}
