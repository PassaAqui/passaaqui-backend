package com.passaaqui.backend.modules.direction.controller;

import com.passaaqui.backend.modules.direction.dto.DirectionRequestDTO;
import com.passaaqui.backend.modules.direction.service.DirectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/direction")
@PreAuthorize("hasAnyRole('TOURIST')")
@RequiredArgsConstructor
public class DirectionController {

    private final DirectionService service;

    @PostMapping
    public ResponseEntity<?> getDirection(@RequestBody @Valid DirectionRequestDTO dto) {
        return ResponseEntity.ok(service.getDirections(dto));
    }
}
