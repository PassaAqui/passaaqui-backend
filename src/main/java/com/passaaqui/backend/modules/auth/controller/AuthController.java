package com.passaaqui.backend.modules.auth.controller;

import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.auth.dto.LoginDTO;
import com.passaaqui.backend.modules.auth.dto.RegisterShopkeeperDTO;
import com.passaaqui.backend.modules.auth.dto.RegisterTouristDTO;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.auth.service.AuthService;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.shared.objects.JWTObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final StorageService storageService;

    @PostMapping("/register/tourist")
    public ResponseEntity<TouristModel> registerAccountTourist(@RequestBody @Valid RegisterTouristDTO dto) {
        TouristModel newTourist = service.registerAccountTourist(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(newTourist);
    }

    @PostMapping(value = "/register/shopkeeper", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ShopkeeperModel> registerAccountShopkeeper(
            @RequestPart("data") @Valid RegisterShopkeeperDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ShopkeeperModel newShopkeeper = service.registerAccountShopkeeper(dto, image);

        return ResponseEntity.status(HttpStatus.CREATED).body(newShopkeeper);
    }

    @PostMapping("/login")
    public ResponseEntity<JWTObject> login(@RequestBody @Valid LoginDTO dto, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        JWTObject tokens = service.loginAccount(dto.email(), dto.password(), userAgent, ipAddress);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/refresh")
    public ResponseEntity<JWTObject> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResourceNotFoundException("Refresh token is missing.");
        }

        String refreshToken = authHeader.substring(7);
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        JWTObject tokens = service.refreshToken(refreshToken, userAgent, ipAddress);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            service.logout(authHeader.substring(7));
        }

        return ResponseEntity.ok().build();
    }
}
