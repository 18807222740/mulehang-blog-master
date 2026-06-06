package com.mulehang.blog.service.impl;

import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.ArticleReadCountService;
import com.mulehang.blog.service.HotArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * 文章阅读量服务：Redis 缓冲增量，定时批量刷入 MySQL，降低读路径 DB 写压力。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleReadCountServiceImpl implements ArticleReadCountService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final BlogArticleMapper articleMapper;
    private final HotArticleService hotArticleService;

    /**
     * 记录一次阅读。
     *
     * @param articleId 文章 ID
     */
    @Override
    public void recordRead(Long articleId) {
        if (articleId == null) {
            return;
        }
        String field = articleId.toString();
        redisTemplate.opsForHash().increment(RedisKeys.ARTICLE_READ_PENDING_HASH, field, 1);
        redisTemplate.opsForSet().add(RedisKeys.ARTICLE_READ_DIRTY_SET, field);
        hotArticleService.incrementReadCount(articleId);
    }

    /**
     * 计算展示阅读量。
     *
     * @param articleId   文章 ID
     * @param dbReadCount 数据库阅读量
     * @return 展示阅读量
     */
    @Override
    public long resolveDisplayCount(Long articleId, Long dbReadCount) {
        long base = dbReadCount == null ? 0L : dbReadCount;
        if (articleId == null) {
            return base;
        }
        Object pending = redisTemplate.opsForHash().get(RedisKeys.ARTICLE_READ_PENDING_HASH, articleId.toString());
        if (pending instanceof Number number) {
            return base + number.longValue();
        }
        if (pending != null) {
            try {
                return base + Long.parseLong(pending.toString());
            } catch (NumberFormatException ignored) {
                return base;
            }
        }
        return base;
    }

    /**
     * 定时将 Redis 待刷盘阅读量写入数据库（默认每 5 分钟）。
     */
    @Override
    @Scheduled(fixedDelayString = "${blog.read-count.flush-interval-ms:300000}")
    public void flushPendingToDatabase() {
        Set<Object> dirtyIds = redisTemplate.opsForSet().members(RedisKeys.ARTICLE_READ_DIRTY_SET);
        if (dirtyIds == null || dirtyIds.isEmpty()) {
            return;
        }
        int flushed = 0;
        for (Object dirtyId : dirtyIds) {
            if (dirtyId == null) {
                continue;
            }
            String field = dirtyId.toString();
            Object pendingObj = redisTemplate.opsForHash().get(RedisKeys.ARTICLE_READ_PENDING_HASH, field);
            long pending = toLong(pendingObj);
            if (pending <= 0) {
                redisTemplate.opsForSet().remove(RedisKeys.ARTICLE_READ_DIRTY_SET, field);
                continue;
            }
            Long articleId = parseArticleId(field);
            if (articleId == null) {
                continue;
            }
            try {
                articleMapper.incrementReadCountBy(articleId, pending);
                redisTemplate.opsForHash().increment(RedisKeys.ARTICLE_READ_PENDING_HASH, field, -pending);
                Object remaining = redisTemplate.opsForHash().get(RedisKeys.ARTICLE_READ_PENDING_HASH, field);
                if (toLong(remaining) <= 0) {
                    redisTemplate.opsForHash().delete(RedisKeys.ARTICLE_READ_PENDING_HASH, field);
                    redisTemplate.opsForSet().remove(RedisKeys.ARTICLE_READ_DIRTY_SET, field);
                }
                flushed++;
            } catch (Exception e) {
                log.warn("刷盘阅读量失败: articleId={}, pending={}", articleId, pending, e);
            }
        }
        if (flushed > 0) {
            log.info("阅读量刷盘完成: count={}", flushed);
        }
    }

    /**
     * 将对象转为 long。
     *
     * @param value 值
     * @return long
     */
    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 解析文章 ID。
     *
     * @param field Redis Hash field
     * @return 文章 ID
     */
    private Long parseArticleId(String field) {
        try {
            return Long.parseLong(field);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
