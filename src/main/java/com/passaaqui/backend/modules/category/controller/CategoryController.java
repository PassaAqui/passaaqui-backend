package com.passaaqui.backend.modules.category.controller;

import com.passaaqui.backend.modules.category.dto.CategoryFeedDTO;
import com.passaaqui.backend.modules.category.dto.CreateCategoryDTO;
import com.passaaqui.backend.modules.category.dto.UpdateCategoryDTO;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<CategoryModel> create(@RequestBody @Valid CreateCategoryDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<CategoryModel>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryFeedDTO> findById(
            @PathVariable Integer id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.findFeedById(id, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<CategoryModel> update(@PathVariable Integer id, @RequestBody UpdateCategoryDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
