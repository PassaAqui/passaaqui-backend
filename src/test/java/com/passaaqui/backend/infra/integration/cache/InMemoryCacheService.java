package com.passaaqui.backend.infra.integration.cache;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("test")
@Primary
public class InMemoryCacheService implements CacheService {

    private final Map<String, Object> store = new ConcurrentHashMap<>();

    @Override
    public void set(String key, Object value) {
        store.put(key, value);
    }

    @Override
    public void setWithTtl(String key, Object value, Duration ttl) {
        store.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = store.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }

    @Override
    public boolean exists(String key) {
        return store.containsKey(key);
    }

    @Override
    public Set<String> keys(String pattern) {
        return store.keySet();
    }
}
