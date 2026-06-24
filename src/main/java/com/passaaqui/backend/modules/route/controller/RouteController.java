package com.passaaqui.backend.modules.route.controller;

import com.passaaqui.backend.modules.route.dto.RouteSessionDTO;
import com.passaaqui.backend.modules.route.dto.StartRouteDTO;
import com.passaaqui.backend.modules.route.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<RouteSessionDTO> start(@RequestBody(required = false) StartRouteDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (dto == null) {
            dto = new StartRouteDTO(null, null);
        }
        return ResponseEntity.ok(routeService.start(authentication.getName(), dto));
    }
}
