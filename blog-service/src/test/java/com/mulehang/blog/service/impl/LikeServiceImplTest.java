package com.mulehang.blog.service.impl;

import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.redis.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LikeServiceImpl 单元测试。
 */
@SpringBootTest(classes = LikeServiceImplTest.EmptyConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class LikeServiceImplTest {

    private RedissonClient redissonClient;

    private RedisTemplate<String, Object> redisTemplate;

    private SetOperations<String, Object> setOperations;

    private BlogArticleMapper articleMapper;

    private LikeServiceImpl likeService;

    /**
     * 初始化测试依赖。
     */
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redissonClient = Mockito.mock(RedissonClient.class);
        redisTemplate = (RedisTemplate<String, Object>) Mockito.mock(RedisTemplate.class);
        setOperations = (SetOperations<String, Object>) Mockito.mock(SetOperations.class);
        articleMapper = Mockito.mock(BlogArticleMapper.class);
        likeService = new LikeServiceImpl(redissonClient, redisTemplate, articleMapper);
    }

    /**
     * 验证点赞成功路径会写入 Redis 并更新点赞数。
     */
    @Test
    void likeArticle_shouldAcquireLockAndUpdateWhenNotLiked() throws InterruptedException {
        Long userId = 1L;
        Long articleId = 2L;
        String lockKey = RedisKeys.LOCK_LIKE_PREFIX + articleId + ":" + userId;
        String likeKey = RedisKeys.ARTICLE_LIKE_SET_PREFIX + articleId;

        RLock lock = org.mockito.Mockito.mock(RLock.class);
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(likeKey, userId.toString())).thenReturn(false);
        when(articleMapper.incrementLikeCount(articleId)).thenReturn(1);

        boolean result = likeService.likeArticle(userId, articleId);

        assertTrue(result);
        verify(articleMapper).incrementLikeCount(articleId);
        verify(setOperations).add(likeKey, userId.toString());
        verify(lock).unlock();
    }

    /**
     * 验证已点赞时不会重复写入或更新。
     */
    @Test
    void likeArticle_shouldReturnFalseWhenAlreadyLiked() throws InterruptedException {
        Long userId = 3L;
        Long articleId = 4L;
        String lockKey = RedisKeys.LOCK_LIKE_PREFIX + articleId + ":" + userId;
        String likeKey = RedisKeys.ARTICLE_LIKE_SET_PREFIX + articleId;

        RLock lock = org.mockito.Mockito.mock(RLock.class);
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(likeKey, userId.toString())).thenReturn(true);

        boolean result = likeService.likeArticle(userId, articleId);

        assertFalse(result);
        verify(setOperations, never()).add(likeKey, userId.toString());
        verify(articleMapper, never()).incrementLikeCount(articleId);
        verify(lock).unlock();
    }

    /**
     * 验证获取锁失败时直接返回 false。
     */
    @Test
    void likeArticle_shouldReturnFalseWhenLockNotAcquired() throws InterruptedException {
        Long userId = 5L;
        Long articleId = 6L;
        String lockKey = RedisKeys.LOCK_LIKE_PREFIX + articleId + ":" + userId;

        RLock lock = org.mockito.Mockito.mock(RLock.class);
        when(redissonClient.getLock(lockKey)).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(false);

        boolean result = likeService.likeArticle(userId, articleId);

        assertFalse(result);
        verify(redisTemplate, never()).opsForSet();
        verify(articleMapper, never()).incrementLikeCount(articleId);
        verify(lock, never()).unlock();
    }

    /**
     * 空配置用于加载 Spring Boot 测试上下文。
     */
    @Configuration
    static class EmptyConfig {
    }
}
