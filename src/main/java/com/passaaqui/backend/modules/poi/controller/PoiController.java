package com.passaaqui.backend.modules.poi.controller;

import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.poi.dto.CheckinRequestDTO;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO;
import com.passaaqui.backend.modules.poi.dto.CreatePoiDTO;
import com.passaaqui.backend.modules.poi.dto.PoiDetailDTO;
import com.passaaqui.backend.modules.poi.dto.PoiNearbyDTO;
import com.passaaqui.backend.modules.poi.dto.UpdatePoiDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.service.PoiCheckinService;
import com.passaaqui.backend.modules.poi.service.PoiService;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.shopkeeper.service.ShopkeeperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pois")
@RequiredArgsConstructor
public class PoiController {

    private final PoiService service;
    private final StorageService storageService;
    private final PoiCheckinService checkinService;
    private final ShopkeeperService shopkeeperService;

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
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String mode,
            Pageable pageable) {

        if (latitude != null && longitude != null) {
            List<PoiNearbyDTO> pois = service.findNearby(latitude, longitude, mode);
            return ResponseEntity.ok(pois);
        }

        Page<PoiModel> pois = service.findAll(pageable);
        for (PoiModel poi : pois) {
            if (poi.getImage() != null) {
                poi.setImageUrl(storageService.getFileUrl(poi.getImage()));
            }
        }
        return ResponseEntity.ok(pois);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoiDetailDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findDetailById(id));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<PoiModel> updateImageShopkeeper(
            @PathVariable Integer id,
            @RequestParam("image") MultipartFile image) {
        PoiModel poi = service.findById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ShopkeeperModel shopkeeper = shopkeeperService.findById(Integer.parseInt(auth.getName()));

        if (poi.getShopkeeper() == null || !poi.getShopkeeper().getId().equals(shopkeeper.getId())) {
            return ResponseEntity.status(403).build();
        }

        PoiModel updated = service.updateImage(id, image);
        if (updated.getImage() != null) {
            updated.setImageUrl(storageService.getFileUrl(updated.getImage()));
        }
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/{id}/image/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<PoiModel> updateImageAdmin(
            @PathVariable Integer id,
            @RequestParam("image") MultipartFile image) {
        PoiModel updated = service.updateImage(id, image);
        if (updated.getImage() != null) {
            updated.setImageUrl(storageService.getFileUrl(updated.getImage()));
        }
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{poiId}/checkin")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<CheckinResponseDTO> checkin(
            @PathVariable Integer poiId,
            @RequestBody @Valid CheckinRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = Integer.parseInt(authentication.getName());
        return ResponseEntity.ok(checkinService.checkin(poiId, userId, request));
    }
}
