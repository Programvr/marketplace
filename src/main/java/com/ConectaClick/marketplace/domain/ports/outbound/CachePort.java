package com.ConectaClick.marketplace.domain.ports.outbound;

import java.util.Optional;
import java.time.Duration;

public interface CachePort {
    <T> Optional<T> get(String key, Class<T> type);
    <T> void put(String key, T value, Duration ttl);
    void evict(String key);
    void evictByPattern(String pattern);
    boolean exists(String key);
}
