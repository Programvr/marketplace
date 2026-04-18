package com.conectaclick.marketplace.infrastructure.nosql.adapters;

import com.conectaclick.marketplace.domain.ports.outbound.CachePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAdapter implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }

            if (type.isInstance(value)) {
                return Optional.of(type.cast(value));
            }

            // Si es un String (JSON), deserializarlo
            if (value instanceof String) {
                T deserializedValue = objectMapper.readValue((String) value, type);
                return Optional.of(deserializedValue);
            }

            log.warn("Cannot cast cached value to type: {}", type.getName());
            return Optional.empty();

        } catch (JsonProcessingException e) {
            log.error("Error deserializing cached value for key: {}", key, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting value from cache for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        try {
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                // Para tipos simples, guardar directamente
                redisTemplate.opsForValue().set(key, value, ttl);
            } else {
                // Para objetos complejos, serializar a JSON
                String jsonValue = objectMapper.writeValueAsString(value);
                redisTemplate.opsForValue().set(key, jsonValue, ttl);
            }
            log.debug("Cached value for key: {} with TTL: {}", key, ttl);
        } catch (JsonProcessingException e) {
            log.error("Error serializing value for key: {}", key, e);
        } catch (Exception e) {
            log.error("Error putting value in cache for key: {}", key, e);
        }
    }

    @Override
    public void evict(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Evicted cache key: {} - Result: {}", key, result);
        } catch (Exception e) {
            log.error("Error evicting cache key: {}", key, e);
        }
    }

    @Override
    public void evictByPattern(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.debug("Evicted {} cache keys matching pattern: {}", deletedCount, pattern);
            }
        } catch (Exception e) {
            log.error("Error evicting cache keys by pattern: {}", pattern, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error checking if cache key exists: {}", key, e);
            return false;
        }
    }
}
