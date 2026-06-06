package com.mulehang.blog.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 缓存失效监听器：收到 Redis 广播后仅淘汰本机 Caffeine L1。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictListener implements MessageListener {

    private final MultiLevelCache multiLevelCache;

    /**
     * 处理 Redis Pub/Sub 缓存失效消息。
     *
     * @param message 消息体（缓存 Key）
     * @param pattern 匹配模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (message == null || message.getBody() == null) {
            return;
        }
        String cacheKey = new String(message.getBody());
        multiLevelCache.evictLocal(cacheKey);
        log.debug("收到缓存失效广播，已清理本地缓存: key={}", cacheKey);
    }
}
