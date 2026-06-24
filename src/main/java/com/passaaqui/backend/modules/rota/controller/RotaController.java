package com.passaaqui.backend.modules.rota.controller;

import com.passaaqui.backend.modules.rota.dto.RotaResponseDTO;
import com.passaaqui.backend.modules.rota.dto.StartRotaDTO;
import com.passaaqui.backend.modules.rota.service.RotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rota")
@RequiredArgsConstructor
public class RotaController {

    private final RotaService rotaService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<RotaResponseDTO> start(@RequestBody @Valid StartRotaDTO dto, Authentication authentication) {
        Integer touristId = Integer.valueOf(authentication.getName());
        return ResponseEntity.ok(rotaService.start(dto, touristId));
    }
}
