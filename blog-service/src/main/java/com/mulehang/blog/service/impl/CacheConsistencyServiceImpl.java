package com.mulehang.blog.service.impl;

import com.mulehang.blog.cache.CacheEvictBroadcaster;
import com.mulehang.blog.cache.MultiLevelCache;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.CacheConsistencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存一致性 Service（Cache-Aside + 延迟双删 + Redis 广播 L1 失效）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheConsistencyServiceImpl implements CacheConsistencyService {

    /**
     * 延迟双删调度器。
     */
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("cache-double-delete");
        t.setDaemon(true);
        return t;
    });

    private static final long SECOND_DELETE_DELAY_MS = 500;

    private final MultiLevelCache multiLevelCache;
    private final CacheEvictBroadcaster cacheEvictBroadcaster;

    /**
     * 清除文章详情缓存（立即 + 延迟双删 + 广播多实例 L1 失效）。
     *
     * @param articleId 文章 ID
     */
    @Override
    public void evictArticleDetail(Long articleId) {
        if (articleId == null) {
            return;
        }
        String key = RedisKeys.ARTICLE_DETAIL_PREFIX + articleId;
        evictAndBroadcast(key, articleId);

        Runnable secondDelete = () -> {
            try {
                evictAndBroadcast(key, articleId);
            } catch (Exception e) {
                log.warn("第二次缓存删除失败, key={}", key, e);
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    SCHEDULER.schedule(secondDelete, SECOND_DELETE_DELAY_MS, TimeUnit.MILLISECONDS);
                }
            });
        } else {
            SCHEDULER.schedule(secondDelete, SECOND_DELETE_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 本地 + Redis 淘汰，并广播其他实例清理 L1。
     *
     * @param key       缓存 Key
     * @param articleId 文章 ID
     */
    private void evictAndBroadcast(String key, Long articleId) {
        multiLevelCache.evict(key);
        cacheEvictBroadcaster.publishArticleDetailEvict(articleId);
    }
}
