package com.passaaqui.backend.infra.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
@Profile("!test")
@RequiredArgsConstructor
public class RedisRateLimiterServiceImpl implements RateLimiterService {

    private final ProxyManager<byte[]> proxyManager;
    private final Map<RateLimitTier, Supplier<BucketConfiguration>> configurationCache = new ConcurrentHashMap<>();

    @Override
    public ConsumptionProbe tryConsume(String key, RateLimitTier tier) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        Supplier<BucketConfiguration> configurationSupplier = configurationCache.computeIfAbsent(
                tier,
                t -> () -> BucketConfiguration.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(t.getCapacity())
                                .refillGreedy(t.getCapacity(), t.getRefillPeriod())
                                .build())
                        .build()
        );

        Bucket bucket = proxyManager.builder().build(keyBytes, configurationSupplier);
        return bucket.tryConsumeAndReturnRemaining(1);
    }
}
