package com.mulehang.blog.cache;

import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 多级缓存（本地缓存 + Redis 缓存）。
 */
@Component
public class MultiLevelCache {

    private static final Duration DEFAULT_REDIS_TTL = Duration.ofMinutes(30);
    private static final String LAYER_LOCAL = "local";
    private static final String LAYER_REDIS = "redis";

    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MultiLevelCacheMetrics metrics;

    /**
     * 构造多级缓存组件。
     * @param localCache 本地缓存
     * @param redisTemplate Redis 操作模板
     * @param meterRegistryProvider MeterRegistry 提供者（可为空）
     */
    public MultiLevelCache(Cache<String, Object> localCache,
                           RedisTemplate<String, Object> redisTemplate,
                           ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.localCache = Objects.requireNonNull(localCache, "localCache 不能为空");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate 不能为空");
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        this.metrics = meterRegistry == null ? MultiLevelCacheMetrics.noop() : new MultiLevelCacheMetrics(meterRegistry);
    }

    /**
     * 根据键从多级缓存中获取值。
     * @param key 键
     * @param type 值的类型
     * @param loader 加载器
     * @param <T> 值的类型
     * @return 缓存中的值，或通过加载器获取的值
     */
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        return get(key, type, DEFAULT_REDIS_TTL, loader);
    }

    /**
     * 根据键从多级缓存中获取值。
     * @param key 键
     * @param type 值的类型
     * @param redisTtl Redis 缓存的过期时间
     * @param loader 加载器
     * @param <T> 值的类型
     * @return 缓存中的值，或通过加载器获取的值
     */
    public <T> T get(String key, Class<T> type, Duration redisTtl, Supplier<T> loader) {
        Objects.requireNonNull(key, "键不能为空");
        Objects.requireNonNull(type, "值类型不能为空");
        Objects.requireNonNull(redisTtl, "Redis 过期时间不能为空");
        Objects.requireNonNull(loader, "加载器不能为空");

        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            if (type.isInstance(localValue)) {
                metrics.recordHit(key, LAYER_LOCAL);
                return type.cast(localValue);
            }
            localCache.invalidate(key);
            metrics.recordMiss(key, LAYER_LOCAL);
        } else {
            metrics.recordMiss(key, LAYER_LOCAL);
        }

        Object redisValue = redisTemplate.opsForValue().get(key);
        if (redisValue != null) {
            if (type.isInstance(redisValue)) {
                localCache.put(key, redisValue);
                metrics.recordHit(key, LAYER_REDIS);
                return type.cast(redisValue);
            }
            redisTemplate.delete(key);
            metrics.recordMiss(key, LAYER_REDIS);
        } else {
            metrics.recordMiss(key, LAYER_REDIS);
        }

        metrics.recordLoad(key);
        T value = loader.get();
        if (value != null) {
            redisTemplate.opsForValue().set(key, value, redisTtl);
            localCache.put(key, value);
        }
        return value;
    }

    /**
     * 从缓存中移除指定的键（L1 + L2）。
     *
     * @param key 键
     */
    public void evict(String key) {
        if (key == null) {
            return;
        }
        evictLocal(key);
        redisTemplate.delete(key);
        metrics.recordEvict(key);
    }

    /**
     * 仅淘汰本地 L1 缓存（用于 Redis Pub/Sub 广播场景）。
     *
     * @param key 键
     */
    public void evictLocal(String key) {
        if (key == null) {
            return;
        }
        localCache.invalidate(key);
        metrics.recordEvict(key);
    }

    /**
     * 根据缓存键提取低基数的缓存名称。
     * @param key 缓存键
     * @return 缓存名称
     */
    private static String resolveCacheName(String key) {
        int index = key.lastIndexOf(':');
        if (index <= 0) {
            return "default";
        }
        return key.substring(0, index);
    }

    /**
     * 多级缓存指标记录器。
     */
    private static final class MultiLevelCacheMetrics {
        private static final String METRIC_HIT = "cache.multilevel.hit";
        private static final String METRIC_MISS = "cache.multilevel.miss";
        private static final String METRIC_LOAD = "cache.multilevel.load";
        private static final String METRIC_EVICT = "cache.multilevel.evict";
        private static final String TAG_LAYER = "layer";
        private static final String TAG_CACHE = "cache";

        private final MeterRegistry meterRegistry;

        /**
         * 创建一个基于 Micrometer 的缓存指标记录器。
         * @param meterRegistry Micrometer 注册表
         */
        private MultiLevelCacheMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        /**
         * 创建一个不执行任何统计的指标记录器。
         * @return 空实现
         */
        private static MultiLevelCacheMetrics noop() {
            return new MultiLevelCacheMetrics(null);
        }

        /**
         * 记录命中次数。
         * @param key 缓存键
         * @param layer 缓存层级
         */
        private void recordHit(String key, String layer) {
            if (meterRegistry == null) {
                return;
            }
            meterRegistry.counter(METRIC_HIT, TAG_LAYER, layer, TAG_CACHE, resolveCacheName(key)).increment();
        }

        /**
         * 记录未命中次数。
         * @param key 缓存键
         * @param layer 缓存层级
         */
        private void recordMiss(String key, String layer) {
            if (meterRegistry == null) {
                return;
            }
            meterRegistry.counter(METRIC_MISS, TAG_LAYER, layer, TAG_CACHE, resolveCacheName(key)).increment();
        }

        /**
         * 记录加载次数。
         * @param key 缓存键
         */
        private void recordLoad(String key) {
            if (meterRegistry == null) {
                return;
            }
            meterRegistry.counter(METRIC_LOAD, TAG_CACHE, resolveCacheName(key)).increment();
        }

        /**
         * 记录删除次数。
         * @param key 缓存键
         */
        private void recordEvict(String key) {
            if (meterRegistry == null) {
                return;
            }
            meterRegistry.counter(METRIC_EVICT, TAG_CACHE, resolveCacheName(key)).increment();
        }
    }
}
