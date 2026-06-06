package com.mulehang.blog.service.impl;

import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.HotArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 热门文章榜 Service。
 *
 * <p>基于 Redis ZSet 实现热榜统计，member 为文章 ID，score 为热度值。</p>
 */
@Service
@RequiredArgsConstructor
public class HotArticleServiceImpl implements HotArticleService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 阅读量 +1。
     *
     * <p>使用 ZINCRBY 命令对指定文章的热度分数进行累加。</p>
     *
     * @param articleId 文章 ID
     */
    @Override
    public void incrementReadCount(Long articleId) {
        if (articleId == null) {
            return;
        }
        redisTemplate.opsForZSet().incrementScore(
                RedisKeys.HOT_ARTICLES_ZSET,
                articleId.toString(),
                1
        );
    }

    /**
     * 获取 TopN 热门文章 ID。
     *
     * <p>使用 ZREVRANGE 命令按分数倒序获取前 N 个文章 ID。</p>
     *
     * @param topN 返回数量
     * @return 热门文章 ID 列表
     */
    @Override
    public List<Long> getHotArticleIds(int topN) {
        if (topN <= 0) {
            return Collections.emptyList();
        }
        Set<Object> ids = redisTemplate.opsForZSet()
                .reverseRange(RedisKeys.HOT_ARTICLES_ZSET, 0, topN - 1);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(Object::toString)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * 每日 0 点重置热门文章榜。
     *
     * <p>通过定时任务清空 Redis 中的热榜数据，实现每日榜单重置。</p>
     */
    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetHotArticles() {
        redisTemplate.delete(RedisKeys.HOT_ARTICLES_ZSET);
    }
}
