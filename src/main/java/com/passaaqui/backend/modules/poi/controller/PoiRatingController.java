package com.passaaqui.backend.modules.poi.controller;

import com.passaaqui.backend.modules.poi.dto.CreatePoiRatingDTO;
import com.passaaqui.backend.modules.poi.model.PoiRatingModel;
import com.passaaqui.backend.modules.poi.service.PoiRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pois/{poiId}/ratings")
@RequiredArgsConstructor
public class PoiRatingController {

    private final PoiRatingService ratingService;

    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<PoiRatingModel> rate(
            @PathVariable Integer poiId,
            @RequestBody @Valid CreatePoiRatingDTO dto) {
        Integer userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.ok(ratingService.rate(poiId, userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<PoiRatingModel>> getRatings(@PathVariable Integer poiId) {
        return ResponseEntity.ok(ratingService.getRatingsByPoiId(poiId));
    }
}
