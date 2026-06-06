package com.mulehang.blog.service.impl;

import com.mulehang.blog.exception.BusinessException;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 点赞 Service（DB 优先 + Redis 去重，保证计数一致性）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BlogArticleMapper articleMapper;

    /**
     * 点赞文章。
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return true=点赞成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeArticle(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            throw BusinessException.badRequest("参数 userId/articleId 不能为空");
        }

        String lockKey = RedisKeys.LOCK_LIKE_PREFIX + articleId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    String likeKey = RedisKeys.ARTICLE_LIKE_SET_PREFIX + articleId;
                    if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, userId.toString()))) {
                        return false;
                    }
                    int updated = articleMapper.incrementLikeCount(articleId);
                    if (updated <= 0) {
                        throw BusinessException.notFound("文章不存在: " + articleId);
                    }
                    redisTemplate.opsForSet().add(likeKey, userId.toString());
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /**
     * 查询是否已点赞。
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return true=已点赞
     */
    @Override
    public boolean hasLiked(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            throw BusinessException.badRequest("参数 userId/articleId 不能为空");
        }
        String likeKey = RedisKeys.ARTICLE_LIKE_SET_PREFIX + articleId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, userId.toString()));
    }

    /**
     * 取消点赞。
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return true=取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeArticle(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            throw BusinessException.badRequest("参数 userId/articleId 不能为空");
        }

        String lockKey = RedisKeys.LOCK_LIKE_PREFIX + articleId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    String likeKey = RedisKeys.ARTICLE_LIKE_SET_PREFIX + articleId;
                    if (!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, userId.toString()))) {
                        return false;
                    }
                    articleMapper.decrementLikeCount(articleId);
                    redisTemplate.opsForSet().remove(likeKey, userId.toString());
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
