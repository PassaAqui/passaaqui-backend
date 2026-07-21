package com.passaaqui.backend.modules.product.controller;

import com.passaaqui.backend.modules.product.dto.CreateProductDTO;
import com.passaaqui.backend.modules.product.dto.UpdateProductDTO;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SHOPKEEPER', 'ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<ProductModel> addImage(
            @PathVariable Integer id,
            @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(service.addImage(id, image));
    }

    @DeleteMapping("/{id}/images/{index}")
    @PreAuthorize("hasAnyRole('SHOPKEEPER', 'ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<ProductModel> removeImage(
            @PathVariable Integer id,
            @PathVariable int index) {
        return ResponseEntity.ok(service.removeImage(id, index));
    }

    @GetMapping
    public ResponseEntity<List<ProductModel>> findAll() {
        return ResponseEntity.ok(service.findAll());
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
