package com.mulehang.blog.cache;

import com.mulehang.blog.redis.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 缓存失效广播器（Redis Pub/Sub），用于多实例间同步 L1 本地缓存淘汰。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictBroadcaster {

    /**
     * Redis 频道：缓存失效通知。
     */
    public static final String CHANNEL = "blog:cache:evict";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 广播文章详情缓存失效。
     *
     * @param articleId 文章 ID
     */
    public void publishArticleDetailEvict(Long articleId) {
        if (articleId == null) {
            return;
        }
        String cacheKey = RedisKeys.ARTICLE_DETAIL_PREFIX + articleId;
        try {
            stringRedisTemplate.convertAndSend(CHANNEL, cacheKey);
            log.debug("已广播缓存失效: key={}", cacheKey);
        } catch (Exception e) {
            log.warn("广播缓存失效失败: key={}", cacheKey, e);
        }
    }
}
