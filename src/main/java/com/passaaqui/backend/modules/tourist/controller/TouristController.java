package com.passaaqui.backend.modules.tourist.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.passaaqui.backend.modules.tourist.dto.UpdateTouristDTO;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.service.TouristService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/tourists", "/api/tourist"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
public class TouristController {

    private final TouristService service;

    @GetMapping
    public ResponseEntity<List<TouristModel>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<TouristModel> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = Integer.parseInt(authentication.getPrincipal().toString());
        return ResponseEntity.ok(service.findById(userId));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<TouristModel> findByIdentifier(@PathVariable String identifier) {
        return ResponseEntity.ok(service.findByIdOrEmail(identifier));
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<TouristModel> update(@PathVariable String identifier, @RequestBody @Valid UpdateTouristDTO dto) {
        return ResponseEntity.ok(service.update(identifier, dto));
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<Void> delete(@PathVariable String identifier) {
        service.delete(identifier);
        return ResponseEntity.noContent().build();
    }
}