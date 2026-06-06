package com.mulehang.blog.service.impl;

import com.mulehang.blog.cache.CacheEvictBroadcaster;
import com.mulehang.blog.cache.MultiLevelCache;
import com.mulehang.blog.redis.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * CacheConsistencyServiceImpl 单元测试。
 */
@SpringBootTest(classes = CacheConsistencyServiceImplTest.EmptyConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CacheConsistencyServiceImplTest {

    private MultiLevelCache multiLevelCache;

    private CacheConsistencyServiceImpl cacheConsistencyService;

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        multiLevelCache = Mockito.mock(MultiLevelCache.class);
        CacheEvictBroadcaster broadcaster = Mockito.mock(CacheEvictBroadcaster.class);
        cacheConsistencyService = new CacheConsistencyServiceImpl(multiLevelCache, broadcaster);
    }

    /**
     * 验证无事务时会进行立即删除与延迟双删。
     *
     * @throws InterruptedException 线程中断异常
     */
    @Test
    void evictArticleDetail_shouldScheduleSecondDeleteWithoutTransaction() throws InterruptedException {
        String key = RedisKeys.ARTICLE_DETAIL_PREFIX + 1L;

        cacheConsistencyService.evictArticleDetail(1L);

        Thread.sleep(650);

        verify(multiLevelCache, times(2)).evict(key);
    }

    /**
     * 验证有事务时会注册 afterCommit 并执行延迟双删。
     *
     * @throws InterruptedException 线程中断异常
     */
    @Test
    void evictArticleDetail_shouldRegisterAfterCommitWhenTransactionActive() throws InterruptedException {
        String key = RedisKeys.ARTICLE_DETAIL_PREFIX + 2L;

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            cacheConsistencyService.evictArticleDetail(2L);

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchronizations.size());

            for (TransactionSynchronization synchronization : synchronizations) {
                synchronization.afterCommit();
                break;
            }

            Thread.sleep(650);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }

        verify(multiLevelCache, times(2)).evict(key);
    }

    /**
     * 空配置用于加载 Spring Boot 测试上下文。
     */
    @Configuration
    static class EmptyConfig {
    }
}
