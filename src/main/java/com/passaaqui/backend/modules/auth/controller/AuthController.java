package com.passaaqui.backend.modules.auth.controller;

import java.util.Arrays;

import com.passaaqui.backend.modules.auth.dto.LoginDTO;
import com.passaaqui.backend.modules.auth.dto.RegisterShopkeeperDTO;
import com.passaaqui.backend.modules.auth.dto.RegisterTouristDTO;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.auth.service.AuthService;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.shared.objects.JWTObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    private void setTokensInCookies(JWTObject tokens, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", tokens.getAccess_token());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // deixar true, caso va pra prod.
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(900); 

        Cookie refreshTokenCookie = new Cookie("refresh_token", tokens.getRefresh_token());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // deixar true, caso va pra prod.
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(604800); 

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    private void clearTokensFromCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

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
    public ResponseEntity<JWTObject> login(@RequestBody @Valid LoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        
        JWTObject tokens = service.loginAccount(dto.email(), dto.password(), userAgent, ipAddress);
        setTokensInCookies(tokens, response);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/refresh")
    public ResponseEntity<JWTObject> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "refresh_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (refreshToken == null) {
            throw new ResourceNotFoundException("Refresh token cookie is missing.");
        }

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        JWTObject tokens = service.refreshToken(refreshToken, userAgent, ipAddress);
        setTokensInCookies(tokens, response);
        
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(cookie -> "refresh_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .ifPresent(token -> {
                        service.logout(token);
                        clearTokensFromCookies(response);
                    });
        }

        return ResponseEntity.ok().build();
    }
}
