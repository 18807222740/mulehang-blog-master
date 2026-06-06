package com.mulehang.blog.service.impl;

import com.mulehang.blog.redis.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HotArticleServiceImpl 单元测试。
 */
@SuppressWarnings("unchecked")
@SpringBootTest(classes = HotArticleServiceImplTest.EmptyConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class HotArticleServiceImplTest {

    private RedisTemplate<String, Object> redisTemplate;

    private ZSetOperations<String, Object> zSetOperations;

    private HotArticleServiceImpl hotArticleService;

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        redisTemplate = (RedisTemplate<String, Object>) Mockito.mock(RedisTemplate.class);
        zSetOperations = (ZSetOperations<String, Object>) Mockito.mock(ZSetOperations.class);
        hotArticleService = new HotArticleServiceImpl(redisTemplate);
    }

    /**
     * 验证热榜阅读量会通过 ZINCRBY 累加。
     */
    @Test
    void incrementReadCount_shouldIncrementScore() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        hotArticleService.incrementReadCount(5L);

        verify(zSetOperations).incrementScore(RedisKeys.HOT_ARTICLES_ZSET, "5", 1);
    }

    /**
     * 验证获取热榜 ID 列表会按分数倒序返回。
     */
    @Test
    void getHotArticleIds_shouldReturnOrderedList() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        LinkedHashSet<Object> ids = new LinkedHashSet<>();
        ids.add("3");
        ids.add("2");
        when(zSetOperations.reverseRange(RedisKeys.HOT_ARTICLES_ZSET, 0, 1)).thenReturn(ids);

        List<Long> result = hotArticleService.getHotArticleIds(2);

        assertEquals(List.of(3L, 2L), result);
    }

    /**
     * 验证热榜重置会删除对应的 Redis ZSet。
     */
    @Test
    void resetHotArticles_shouldDeleteKey() {
        hotArticleService.resetHotArticles();

        verify(redisTemplate).delete(RedisKeys.HOT_ARTICLES_ZSET);
    }

    /**
     * 空配置用于加载 Spring Boot 测试上下文。
     */
    @Configuration
    static class EmptyConfig {
    }
}
