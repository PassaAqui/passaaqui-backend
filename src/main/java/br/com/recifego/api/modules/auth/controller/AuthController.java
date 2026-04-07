package br.com.recifego.api.modules.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.recifego.api.modules.auth.dto.LoginDTO;
import br.com.recifego.api.modules.auth.dto.RefreshTokenDTO;
import br.com.recifego.api.modules.auth.dto.RegisterShopkeeperDTO;
import br.com.recifego.api.modules.auth.dto.RegisterTouristDTO;
import br.com.recifego.api.modules.auth.service.AuthService;
import br.com.recifego.api.modules.shopkeeper.model.ShopkeeperModel;
import br.com.recifego.api.modules.tourist.model.TouristModel;
import br.com.recifego.api.shared.objects.JWTObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register/tourist")
    public ResponseEntity<TouristModel> registerAccountTourist(@RequestBody @Valid RegisterTouristDTO dto) {
        TouristModel newTourist = service.registerAccountTourist(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(newTourist);
    }

    @PostMapping("/register/shopkeeper")
    public ResponseEntity<ShopkeeperModel> registerAccountShopkeeper(@RequestBody @Valid RegisterShopkeeperDTO dto) {
        ShopkeeperModel newShopkeeper = service.registerAccountShopkeeper(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(newShopkeeper);
    }

    @PostMapping("/login")
    public ResponseEntity<JWTObject> login(@RequestBody @Valid LoginDTO dto, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        
        JWTObject tokens = service.loginAccount(dto.email(), dto.password(), userAgent, ipAddress);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JWTObject> refresh(@RequestBody @Valid RefreshTokenDTO dto, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        
        JWTObject tokens = service.refreshToken(dto.refreshToken(), userAgent, ipAddress);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenDTO dto) {
        service.logout(dto.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
