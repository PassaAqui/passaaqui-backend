package com.passaaqui.backend.infra.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("test")
public class InMemoryRateLimiterServiceImpl implements RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public ConsumptionProbe tryConsume(String key, RateLimitTier tier) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(tier.getCapacity())
                        .refillGreedy(tier.getCapacity(), tier.getRefillPeriod())
                        .build())
                .build());

        return bucket.tryConsumeAndReturnRemaining(1);
    }

    public void clear() {
        buckets.clear();
    }
}
