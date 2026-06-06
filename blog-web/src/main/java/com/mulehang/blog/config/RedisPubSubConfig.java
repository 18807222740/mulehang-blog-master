package com.mulehang.blog.config;

import com.mulehang.blog.cache.CacheEvictBroadcaster;
import com.mulehang.blog.cache.CacheEvictListener;
import com.mulehang.blog.websocket.WebSocketBroadcastNotifyListener;
import com.mulehang.blog.websocket.WebSocketNotifyBroadcaster;
import com.mulehang.blog.websocket.WebSocketUserNotifyListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Pub/Sub 配置（缓存失效广播、WebSocket 多实例通知）。
 */
@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    /**
     * 字符串 Redis 模板（Pub/Sub 纯文本消息）。
     *
     * @param connectionFactory Redis 连接工厂
     * @return StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis 消息监听容器。
     *
     * @param connectionFactory              Redis 连接工厂
     * @param cacheEvictListener             缓存失效监听
     * @param webSocketUserNotifyListener    WebSocket 定向通知监听
     * @param webSocketBroadcastNotifyListener WebSocket 广播监听
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            CacheEvictListener cacheEvictListener,
            WebSocketUserNotifyListener webSocketUserNotifyListener,
            WebSocketBroadcastNotifyListener webSocketBroadcastNotifyListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(new MessageListenerAdapter(cacheEvictListener),
                new PatternTopic(CacheEvictBroadcaster.CHANNEL));
        container.addMessageListener(new MessageListenerAdapter(webSocketUserNotifyListener),
                new PatternTopic(WebSocketNotifyBroadcaster.USER_CHANNEL));
        container.addMessageListener(new MessageListenerAdapter(webSocketBroadcastNotifyListener),
                new PatternTopic(WebSocketNotifyBroadcaster.BROADCAST_CHANNEL));
        return container;
    }
}
