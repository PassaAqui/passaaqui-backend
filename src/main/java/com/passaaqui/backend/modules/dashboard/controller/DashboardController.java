package com.passaaqui.backend.modules.dashboard.controller;

import com.passaaqui.backend.modules.dashboard.dto.DashboardDTO;
import com.passaaqui.backend.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<DashboardDTO> getDashboard() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Integer shopkeeperId = Integer.parseInt(userId);
        return ResponseEntity.ok(dashboardService.getDashboard(shopkeeperId));
    }
}