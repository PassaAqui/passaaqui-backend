package br.com.recifego.api.modules.tourist.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.recifego.api.modules.tourist.dto.UpdateTouristDTO;
import br.com.recifego.api.modules.tourist.model.TouristModel;
import br.com.recifego.api.modules.tourist.service.TouristService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tourists")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class TouristController {

    private final TouristService service;

    @GetMapping
    public ResponseEntity<List<TouristModel>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<TouristModel> findByIdentifier(@PathVariable String identifier) {
        return ResponseEntity.ok(service.findByIdOrEmail(identifier));
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<TouristModel> update(@PathVariable String identifier, @RequestBody UpdateTouristDTO dto) {
        return ResponseEntity.ok(service.update(identifier, dto));
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<Void> delete(@PathVariable String identifier) {
        service.delete(identifier);
        return ResponseEntity.noContent().build();
    }
}