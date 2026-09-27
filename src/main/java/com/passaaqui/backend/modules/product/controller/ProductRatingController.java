package com.passaaqui.backend.modules.product.controller;

import com.passaaqui.backend.modules.product.dto.ProductRatingResponseDTO;
import com.passaaqui.backend.modules.product.service.ProductRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/ratings")
@RequiredArgsConstructor
public class ProductRatingController {

    private final ProductRatingService ratingService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<ProductRatingResponseDTO> rate(
            @PathVariable Integer productId,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "order_id", required = false) String orderId,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(value = "video", required = false) MultipartFile video) {
        Integer userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.ok(ratingService.rate(productId, userId, rating, comment, orderId, photos, video));
    }

    @GetMapping
    public ResponseEntity<List<ProductRatingResponseDTO>> getRatings(@PathVariable Integer productId) {
        return ResponseEntity.ok(ratingService.getRatingsByProductId(productId));
    }
}