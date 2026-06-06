package com.mulehang.blog.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

/**
 * 多级缓存 L1（Caffeine）配置。
 *
 * <p>统一使用 {@link MultiLevelCache} 手动管理缓存，不再启用 Spring Cache 注解层。</p>
 */
@Configuration
public class CacheConfig {

    /**
     * 多级缓存中的 L1 本地缓存（Caffeine）。
     *
     * @return 本地缓存实例
     */
    @Bean
    public Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();
    }
}
