package com.passaaqui.backend.infra.ratelimit;

import java.time.Duration;

public enum RateLimitTier {
    // 10 requests per minute per IP for authentication (login, registration, password recovery)
    AUTH(10, Duration.ofMinutes(1)),

    // 15 requests per minute per user/IP for order and checkout operations
    ORDER(15, Duration.ofMinutes(1)),

    // 20 requests per minute per user/IP for route calculation and external directions
    DIRECTIONS(20, Duration.ofMinutes(1)),

    // 5 requests per minute per user/IP for POI check-in to prevent GPS spoofing / spam
    CHECKIN(5, Duration.ofMinutes(1)),

    // 100 requests per minute per user/IP as general API fallback
    DEFAULT(100, Duration.ofMinutes(1));

    private final long capacity;
    private final Duration refillPeriod;

    RateLimitTier(long capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillPeriod = refillPeriod;
    }

    public long getCapacity() {
        return capacity;
    }

    public Duration getRefillPeriod() {
        return refillPeriod;
    }
}
