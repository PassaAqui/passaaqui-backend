package com.passaaqui.backend.modules.user.controller;

import java.util.List;

import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.user.dto.UpdateUserDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.passaaqui.backend.modules.user.model.UserModel;
import com.passaaqui.backend.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
public class UserController {

    private final UserService service;
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<UserModel>> findAll() {
        List<UserModel> users = service.findAll();
        for (UserModel user : users) {
            if (user.getImage() != null) {
                user.setImageUrl(storageService.getFileUrl(user.getImage()));
            }
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<UserModel> findByIdentifier(@PathVariable String identifier) {
        UserModel user = service.findByIdOrEmail(identifier);
        if (user.getImage() != null) {
            user.setImageUrl(storageService.getFileUrl(user.getImage()));
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserModel> update(
            @PathVariable Integer id,
            @RequestPart("data") UpdateUserDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        UserModel user = service.update(id, dto, image);
        if (user.getImage() != null) {
            user.setImageUrl(storageService.getFileUrl(user.getImage()));
        }
        return ResponseEntity.ok(user);
    }
}
