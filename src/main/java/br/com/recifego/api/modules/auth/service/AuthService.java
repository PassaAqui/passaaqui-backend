package br.com.recifego.api.modules.auth.service;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.recifego.api.infra.exception.InvalidRequestException;
import br.com.recifego.api.modules.auth.dto.RegisterShopkeeperDTO;
import br.com.recifego.api.modules.auth.dto.RegisterTouristDTO;
import br.com.recifego.api.modules.auth.model.AuthModel;
import br.com.recifego.api.modules.auth.repository.AuthRepository;
import br.com.recifego.api.modules.shopkeeper.model.ShopkeeperModel;
import br.com.recifego.api.modules.shopkeeper.service.ShopkeeperService;
import br.com.recifego.api.modules.tourist.model.TouristModel;
import br.com.recifego.api.modules.tourist.service.TouristService;
import br.com.recifego.api.modules.user.model.UserModel;
import br.com.recifego.api.modules.user.service.UserService;
import br.com.recifego.api.shared.objects.JWTObject;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private final PasswordEncoder passwordEncoder;

    private final TouristService touristService;
    private final ShopkeeperService shopkeeperService;
    private final UserService userService;
    private final AuthRepository authRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    
    private JWTObject generateTokens(UserModel user, String deviceId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return new JWTObject() {{
            setAccess_token(Jwts.builder()
                    .setSubject(user.getId().toString())
                    .claim("role", user.getRole())
                    .claim("deviceId", deviceId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                    .signWith(key) 
                    .compact());
            
            setRefresh_token(Jwts.builder()
                    .setSubject(user.getId().toString())
                    .claim("role", user.getRole())
                    .claim("deviceId", deviceId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                    .signWith(key) 
                    .compact());
        }};
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }

    public TouristModel registerAccountTourist(RegisterTouristDTO dto) {
        if (!dto.password().equalsIgnoreCase(dto.confirm_password())) 
            throw new InvalidRequestException("The passwords provided are not the same.");

        String password_hash = passwordEncoder.encode(dto.password());
        
        TouristModel newTourist = touristService.createUser(dto.email(), dto.name(), password_hash, dto.documentId());

        return newTourist;
    }

    public ShopkeeperModel registerAccountShopkeeper(RegisterShopkeeperDTO dto) {
        if (!dto.password().equalsIgnoreCase(dto.confirm_password())) 
            throw new InvalidRequestException("The passwords provided are not the same.");

        String password_hash = passwordEncoder.encode(dto.password());

        ShopkeeperModel newShopkeeper = shopkeeperService.createUser(dto.email(), dto.name(), password_hash, dto.documentId(), dto.companyName());

        return newShopkeeper;
    }

    public JWTObject loginAccount(String email, String password, String userAgent, String ipAddress) {
        UserModel user = userService.findByEmail(email);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidRequestException("Invalid email or password.");
        }

        String deviceId = UUID.randomUUID().toString();
        JWTObject tokens = generateTokens(user, deviceId);
        
        AuthModel newAuth = new AuthModel();
        newAuth.setUser(user);
        newAuth.setRefreshTokenHash(hashToken(tokens.getRefresh_token()));
        newAuth.setDeviceId(deviceId);
        newAuth.setUserAgent(userAgent);
        newAuth.setIpAddress(ipAddress);
        newAuth.setRevoked(false);
        newAuth.setExpiresAt(LocalDateTime.now().plusDays(7)); 
        
        authRepository.save(newAuth);

        return tokens;

    }

    public JWTObject refreshToken(String refreshToken, String userAgent, String ipAddress) {
        String deviceId;
        try {
            var claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
            deviceId = claims.get("deviceId", String.class);
        } catch (Exception e) {
            throw new InvalidRequestException("Invalid or expired refresh token.");
        }

        AuthModel authModel = authRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new InvalidRequestException("Session not found."));

        if (authModel.isRevoked()) {
            throw new InvalidRequestException("Session revoked. Please login again.");
        }

        if (!hashToken(refreshToken).equals(authModel.getRefreshTokenHash())) {
            authModel.setRevoked(true);
            authRepository.save(authModel);
            throw new InvalidRequestException("Invalid refresh token. Session revoked for security.");
        }

        UserModel user = authModel.getUser();
        JWTObject tokens = generateTokens(user, deviceId);

        authModel.setRefreshTokenHash(hashToken(tokens.getRefresh_token()));
        authModel.setUserAgent(userAgent);
        authModel.setIpAddress(ipAddress);
        authModel.setExpiresAt(LocalDateTime.now().plusDays(7)); 
        
        authRepository.save(authModel);

        return tokens;
    }

    public void logout(String refreshToken) {
        String deviceId = null;
        try {
            var claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
            deviceId = claims.get("deviceId", String.class);
        } catch (ExpiredJwtException e) {
            deviceId = e.getClaims().get("deviceId", String.class);
        } catch (Exception e) {
            throw new InvalidRequestException("Invalid token format.");
        }

        if (deviceId != null) {
            authRepository.findByDeviceId(deviceId).ifPresent(authModel -> {
                authModel.setRevoked(true);
                authRepository.save(authModel);
            });
        }
    }
}
