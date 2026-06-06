package com.mulehang.blog.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * 用户启用状态本地缓存，减少 JWT 过滤器对数据库的频繁查询。
 */
@Service
public class UserStatusCacheService {

    private final Cache<Long, Boolean> activeCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(3))
            .build();

    /**
     * 判断用户是否处于启用状态（带本地缓存）。
     *
     * @param userId 用户 ID
     * @param loader 缓存未命中时的加载逻辑
     * @return true=启用
     */
    public boolean isUserActive(Long userId, BooleanSupplier loader) {
        if (userId == null) {
            return false;
        }
        return Boolean.TRUE.equals(activeCache.get(userId, id -> loader.getAsBoolean()));
    }

    /**
     * 失效指定用户的缓存（禁用账号、修改状态时调用）。
     *
     * @param userId 用户 ID
     */
    public void invalidate(Long userId) {
        if (userId != null) {
            activeCache.invalidate(userId);
        }
    }

    /**
     * 清空全部用户状态缓存。
     */
    public void invalidateAll() {
        activeCache.invalidateAll();
    }
}
