package com.mulehang.blog.service.impl;

import com.mulehang.blog.service.OAuthStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * OAuth state 管理实现（Redis）。
 */
@Service
@RequiredArgsConstructor
public class OAuthStateServiceImpl implements OAuthStateService {

    private static final String KEY_PREFIX = "oauth:state:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存 state。
     *
     * @param state state 值
     */
    @Override
    public void storeState(String state) {
        if (!StringUtils.hasText(state)) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + state, "1", TTL);
    }

    /**
     * 校验并消费 state。
     *
     * @param state state 值
     * @return true-校验通过，false-无效或过期
     */
    @Override
    public boolean consumeState(String state) {
        if (!StringUtils.hasText(state)) {
            return false;
        }
        String key = KEY_PREFIX + state;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
