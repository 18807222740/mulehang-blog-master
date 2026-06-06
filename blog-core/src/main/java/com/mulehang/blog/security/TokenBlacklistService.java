package com.mulehang.blog.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * JWT 黑名单服务（用于主动失效 Token）。
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 将 jti 加入黑名单。
     *
     * @param jti        JWT ID
     * @param ttlSeconds 过期时间（秒）
     */
    public void blacklist(String jti, long ttlSeconds) {
        if (!StringUtils.hasText(jti) || ttlSeconds <= 0) {
            return;
        }
        String key = KEY_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 判断 jti 是否已在黑名单中。
     *
     * @param jti JWT ID
     * @return true-已失效，false-可用
     */
    public boolean isBlacklisted(String jti) {
        if (!StringUtils.hasText(jti)) {
            return false;
        }
        Boolean exists = redisTemplate.hasKey(KEY_PREFIX + jti);
        return Boolean.TRUE.equals(exists);
    }
}
