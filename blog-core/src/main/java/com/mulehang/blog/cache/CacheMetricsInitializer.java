package com.mulehang.blog.cache;

import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 缓存指标初始化器（将多级缓存 L1 Caffeine 统计暴露给 Micrometer）。
 */
@Component
public class CacheMetricsInitializer implements SmartInitializingSingleton {

    private static final String MULTI_LEVEL_LOCAL_CACHE = "multilevel_local";

    private final Cache<String, Object> localCache;
    private final MeterRegistry meterRegistry;

    /**
     * 构造缓存指标初始化器。
     *
     * @param localCache            多级缓存的 L1 本地缓存
     * @param meterRegistryProvider MeterRegistry 提供者（可为空）
     */
    public CacheMetricsInitializer(Cache<String, Object> localCache,
                                   ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.localCache = Objects.requireNonNull(localCache, "localCache 不能为空");
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
    }

    /**
     * 在所有单例初始化完成后注册缓存指标。
     */
    @Override
    public void afterSingletonsInstantiated() {
        if (meterRegistry == null) {
            return;
        }
        CaffeineCacheMetrics.monitor(
                meterRegistry,
                localCache,
                MULTI_LEVEL_LOCAL_CACHE,
                Tags.of("cache_manager", "caffeine", "name", MULTI_LEVEL_LOCAL_CACHE)
        );
    }
}
