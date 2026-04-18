package com.passaaqui.backend.modules.admin.controller;

import com.passaaqui.backend.modules.admin.dto.UpdateAdminDTO;
import com.passaaqui.backend.modules.admin.model.AdminModel;
import com.passaaqui.backend.modules.admin.service.AdminService;
import com.passaaqui.backend.modules.auth.dto.RegisterAdminDTO;
import com.passaaqui.backend.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_ROOT')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<AdminModel> create(@RequestBody @Valid RegisterAdminDTO dto) {
        return ResponseEntity.ok(authService.registerAccountAdmin(dto));
    }

    @GetMapping
    public ResponseEntity<List<AdminModel>> getAll() {
        return ResponseEntity.ok(adminService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminModel> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getOneById(id));
    }

    @GetMapping("/email")
    public ResponseEntity<AdminModel> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(adminService.getOneByEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminModel> update(@PathVariable Integer id,
                                             @RequestBody @Valid UpdateAdminDTO dto) {
        return ResponseEntity.ok(
                adminService.updateAdmin(
                        id,
                        dto.email(),
                        dto.name(),
                        dto.password(),
                        dto.adminType()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}