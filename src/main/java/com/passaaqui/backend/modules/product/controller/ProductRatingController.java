package com.passaaqui.backend.modules.product.controller;

import com.passaaqui.backend.modules.product.dto.CreateProductRatingDTO;
import com.passaaqui.backend.modules.product.model.ProductRatingModel;
import com.passaaqui.backend.modules.product.service.ProductRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/ratings")
@RequiredArgsConstructor
public class ProductRatingController {

    private final ProductRatingService ratingService;

    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<ProductRatingModel> rate(
            @PathVariable Integer productId,
            @RequestBody @Valid CreateProductRatingDTO dto) {
        Integer userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.ok(ratingService.rate(productId, userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<ProductRatingModel>> getRatings(@PathVariable Integer productId) {
        return ResponseEntity.ok(ratingService.getRatingsByProductId(productId));
    }
}