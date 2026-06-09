package com.passaaqui.backend.modules.poi.controller;

import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.poi.dto.CreatePoiDTO;
import com.passaaqui.backend.modules.poi.dto.UpdatePoiDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.service.PoiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pois")
@RequiredArgsConstructor
public class PoiController {

    private final PoiService service;
    private final StorageService storageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<PoiModel> create(
            @RequestPart("data") @Valid CreatePoiDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        PoiModel poi = service.create(dto, image);
        if (poi.getImage() != null) {
            poi.setImageUrl(storageService.getFileUrl(poi.getImage()));
        }
        return ResponseEntity.ok(poi);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<PoiModel> update(@PathVariable Integer id, @RequestBody UpdatePoiDTO dto) {
        PoiModel poi = service.update(id, dto);
        if (poi.getImage() != null) {
            poi.setImageUrl(storageService.getFileUrl(poi.getImage()));
        }
        return ResponseEntity.ok(poi);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<PoiModel>> findAll(Pageable pageable) {
        Page<PoiModel> pois = service.findAll(pageable);
        for (PoiModel poi : pois) {
            if (poi.getImage() != null) {
                poi.setImageUrl(storageService.getFileUrl(poi.getImage()));
            }
        }
        return ResponseEntity.ok(pois);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoiModel> findById(@PathVariable Integer id) {
        PoiModel poi = service.findById(id);
        if (poi.getImage() != null) {
            poi.setImageUrl(storageService.getFileUrl(poi.getImage()));
        }
        return ResponseEntity.ok(poi);
    }
}
