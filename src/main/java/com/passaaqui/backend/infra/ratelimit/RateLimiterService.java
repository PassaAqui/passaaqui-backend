package com.passaaqui.backend.infra.ratelimit;

import io.github.bucket4j.ConsumptionProbe;

public interface RateLimiterService {

    ConsumptionProbe tryConsume(String key, RateLimitTier tier);
}
