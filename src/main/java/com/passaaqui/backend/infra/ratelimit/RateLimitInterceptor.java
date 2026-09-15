package com.passaaqui.backend.infra.ratelimit;

import com.passaaqui.backend.infra.exception.TooManyRequestsException;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        RateLimitTier tier = resolveTier(request, handler);
        if (tier == null) {
            return true;
        }

        String clientKey = resolveClientKey(request, tier);
        ConsumptionProbe probe = rateLimiterService.tryConsume(clientKey, tier);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.setHeader("X-Rate-Limit-Limit", String.valueOf(tier.getCapacity()));
            return true;
        }

        long waitSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        response.setHeader("Retry-After", String.valueOf(waitSeconds));
        response.setHeader("X-Rate-Limit-Remaining", "0");
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(tier.getCapacity()));

        throw new TooManyRequestsException(
                "Taxa de requisições excedida. Tente novamente em " + waitSeconds + " segundos.",
                waitSeconds
        );
    }

    private RateLimitTier resolveTier(HttpServletRequest request, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            RateLimited methodAnnotation = handlerMethod.getMethodAnnotation(RateLimited.class);
            if (methodAnnotation != null) {
                return methodAnnotation.value();
            }

            RateLimited classAnnotation = handlerMethod.getBeanType().getAnnotation(RateLimited.class);
            if (classAnnotation != null) {
                return classAnnotation.value();
            }
        }

        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/")) {
            return null;
        }

        if (uri.startsWith("/api/auth")) {
            return RateLimitTier.AUTH;
        }
        if (uri.contains("/checkin")) {
            return RateLimitTier.CHECKIN;
        }
        if (uri.startsWith("/api/directions") || uri.startsWith("/api/route")) {
            return RateLimitTier.DIRECTIONS;
        }
        if (uri.startsWith("/api/orders")) {
            return RateLimitTier.ORDER;
        }

        return RateLimitTier.DEFAULT;
    }

    private String resolveClientKey(HttpServletRequest request, RateLimitTier tier) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String identifier;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            identifier = "user:" + auth.getName();
        } else {
            identifier = "ip:" + getClientIp(request);
        }
        return "rate_limit:" + tier.name().toLowerCase() + ":" + identifier;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
