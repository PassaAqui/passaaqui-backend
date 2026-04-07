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

    @PostMapping("/refresh")
    public ResponseEntity<JWTObject> refresh(@RequestBody @Valid RefreshTokenDTO dto, HttpServletRequest request, HttpServletResponse response) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        
        JWTObject tokens = service.refreshToken(dto.refreshToken(), userAgent, ipAddress);
        setTokensInCookies(tokens, response);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenDTO dto, HttpServletResponse response) {
        service.logout(dto.refreshToken());
        clearTokensFromCookies(response);
        return ResponseEntity.noContent().build();
    }

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
}
