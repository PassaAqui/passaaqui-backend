package com.passaaqui.backend.infra.integration.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public interface CacheService {

    void set(String key, Object value);

    void setWithTtl(String key, Object value, Duration ttl);

    <T> Optional<T> get(String key, Class<T> type);

    void delete(String key);

    boolean exists(String key);

    Set<String> keys(String pattern);
}
