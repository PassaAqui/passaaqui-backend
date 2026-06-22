package com.passaaqui.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    String userId = claims.getSubject();
                    String role = claims.get("role", String.class);

                    if (userId != null && role != null) {
                        var authority = new SimpleGrantedAuthority("ROLE_" + role);
                        var authentication = new UsernamePasswordAuthenticationToken(
                                userId, null, Collections.singletonList(authority)
                        );
                        accessor.setUser(authentication);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid or expired token");
                }
            }
        }

        return message;
    }
}
