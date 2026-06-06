package com.mulehang.blog.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MultiLevelCache 单元测试。
 */
@SpringBootTest(classes = MultiLevelCacheTest.EmptyConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MultiLevelCacheTest {

    private Cache<String, Object> localCache;

    private RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> valueOperations;

    private MultiLevelCache multiLevelCache;

    /**
     * 初始化测试依赖。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeEach
    void setUp() {
        localCache = (Cache<String, Object>) Mockito.mock(Cache.class);
        redisTemplate = (RedisTemplate<String, Object>) Mockito.mock(RedisTemplate.class);
        valueOperations = (ValueOperations<String, Object>) Mockito.mock(ValueOperations.class);
        ObjectProvider<?> meterRegistryProvider = Mockito.mock(ObjectProvider.class);
        when(meterRegistryProvider.getIfAvailable()).thenReturn(null);

        try {
            multiLevelCache = MultiLevelCache.class
                    .getConstructor(Cache.class, RedisTemplate.class, ObjectProvider.class)
                    .newInstance(localCache, redisTemplate, (ObjectProvider) meterRegistryProvider);
        } catch (NoSuchMethodException e) {
            try {
                multiLevelCache = MultiLevelCache.class
                        .getConstructor(Cache.class, RedisTemplate.class)
                        .newInstance(localCache, redisTemplate);
            } catch (Exception ex) {
                throw new IllegalStateException("无法构造 MultiLevelCache", ex);
            }
        } catch (Exception e) {
            throw new IllegalStateException("无法构造 MultiLevelCache", e);
        }
    }

    /**
     * 验证本地缓存命中时直接返回结果且不访问 Redis。
     */
    @Test
    void get_shouldReturnLocalValueWhenLocalHit() {
        String key = "article:1";
        AtomicBoolean called = new AtomicBoolean(false);
        Supplier<String> loader = () -> {
            called.set(true);
            return "loaded";
        };
        when(localCache.getIfPresent(key)).thenReturn("local");

        String result = multiLevelCache.get(key, String.class, loader);

        assertEquals("local", result);
        assertFalse(called.get());
        verify(redisTemplate, never()).opsForValue();
    }

    /**
     * 验证本地未命中时会返回 Redis 中的值并回填本地缓存。
     */
    @Test
    void get_shouldReturnRedisValueWhenRedisHit() {
        String key = "article:2";
        AtomicBoolean called = new AtomicBoolean(false);
        Supplier<String> loader = () -> {
            called.set(true);
            return "loaded";
        };
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("redis");

        String result = multiLevelCache.get(key, String.class, loader);

        assertEquals("redis", result);
        assertFalse(called.get());
        verify(localCache).put(key, "redis");
    }

    /**
     * 验证本地缓存类型不匹配时会失效本地并回源 Redis。
     */
    @Test
    void get_shouldFallbackToRedisWhenLocalTypeMismatch() {
        String key = "article:3";
        AtomicBoolean called = new AtomicBoolean(false);
        Supplier<String> loader = () -> {
            called.set(true);
            return "loaded";
        };
        when(localCache.getIfPresent(key)).thenReturn(123);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("redis");

        String result = multiLevelCache.get(key, String.class, loader);

        assertEquals("redis", result);
        assertFalse(called.get());
        verify(localCache).invalidate(key);
        verify(localCache).put(key, "redis");
        verify(redisTemplate, never()).delete(key);
    }

    /**
     * 验证 Redis 类型不匹配时会删除 Redis 并回源加载。
     */
    @Test
    void get_shouldReloadWhenRedisTypeMismatch() {
        String key = "article:4";
        Duration ttl = Duration.ofMinutes(5);
        AtomicInteger calls = new AtomicInteger(0);
        Supplier<String> loader = () -> {
            calls.incrementAndGet();
            return "loaded";
        };
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(123);

        String result = multiLevelCache.get(key, String.class, ttl, loader);

        assertEquals("loaded", result);
        assertEquals(1, calls.get());
        verify(redisTemplate).delete(key);
        verify(valueOperations).set(key, "loaded", ttl);
        verify(localCache).put(key, "loaded");
    }

    /**
     * 验证缓存双端未命中时会回源加载并写入缓存。
     */
    @Test
    void get_shouldLoadAndCacheWhenMiss() {
        String key = "article:5";
        AtomicInteger calls = new AtomicInteger(0);
        Supplier<String> loader = () -> {
            calls.incrementAndGet();
            return "loaded";
        };
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        String result = multiLevelCache.get(key, String.class, loader);

        assertEquals("loaded", result);
        assertEquals(1, calls.get());
        verify(valueOperations).set(eq(key), eq("loaded"), any(Duration.class));
        verify(localCache).put(key, "loaded");
    }

    /**
     * 验证回源结果为空时不会写入缓存。
     */
    @Test
    void get_shouldReturnNullWhenLoaderReturnsNull() {
        String key = "article:6";
        AtomicInteger calls = new AtomicInteger(0);
        Supplier<String> loader = () -> {
            calls.incrementAndGet();
            return null;
        };
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        String result = multiLevelCache.get(key, String.class, loader);

        assertNull(result);
        assertEquals(1, calls.get());
        verify(valueOperations, never()).set(eq(key), any(), any(Duration.class));
        verify(localCache, never()).put(eq(key), any());
    }

    /**
     * 验证删除缓存时会同时清理本地与 Redis。
     */
    @Test
    void evict_shouldRemoveLocalAndRedis() {
        String key = "article:7";

        multiLevelCache.evict(key);

        verify(localCache).invalidate(key);
        verify(redisTemplate).delete(key);
    }

    /**
     * 空配置用于加载 Spring Boot 测试上下文。
     */
    @Configuration
    static class EmptyConfig {
    }
}
