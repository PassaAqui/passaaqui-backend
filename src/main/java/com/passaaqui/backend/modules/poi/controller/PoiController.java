package com.passaaqui.backend.modules.poi.controller;

import com.passaaqui.backend.modules.poi.dto.CreatePoiDTO;
import com.passaaqui.backend.modules.poi.dto.CreatePoiRatingDTO;
import com.passaaqui.backend.modules.poi.dto.UpdatePoiDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.PoiRatingModel;
import com.passaaqui.backend.modules.poi.service.PoiRatingService;
import com.passaaqui.backend.modules.poi.service.PoiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pois")
@RequiredArgsConstructor
public class PoiController {

    private final PoiService service;
    private final PoiRatingService ratingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<PoiModel> create(@RequestBody @Valid CreatePoiDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<PoiModel> update(@PathVariable Integer id, @RequestBody UpdatePoiDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PoiModel>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoiModel> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping("/{poiId}/ratings")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<PoiRatingModel> rate(
            @PathVariable Integer poiId,
            @RequestBody @Valid CreatePoiRatingDTO dto) {
        Integer userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.ok(ratingService.rate(poiId, userId, dto));
    }

    @GetMapping("/{poiId}/ratings")
    public ResponseEntity<List<PoiRatingModel>> getRatings(@PathVariable Integer poiId) {
        return ResponseEntity.ok(ratingService.getRatingsByPoiId(poiId));
    }
}
