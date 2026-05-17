package com.passaaqui.backend.modules.product.controller;

import com.passaaqui.backend.modules.product.dto.CreateProductDTO;
import com.passaaqui.backend.modules.product.dto.UpdateProductDTO;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('SHOPKEEPER', 'ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<ProductModel> create(@RequestBody @Valid CreateProductDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SHOPKEEPER', 'ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<ProductModel> update(@PathVariable Integer id, @RequestBody UpdateProductDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SHOPKEEPER', 'ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ProductModel>> getRecentProducts() {
        return ResponseEntity.ok(service.getRecentProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
