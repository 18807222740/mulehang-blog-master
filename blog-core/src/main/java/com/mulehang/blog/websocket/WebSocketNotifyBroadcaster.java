package com.mulehang.blog.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulehang.blog.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 通知广播器（Redis Pub/Sub），支持多实例水平扩展。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotifyBroadcaster {

    /**
     * Redis 频道：用户定向 WebSocket 通知。
     */
    public static final String USER_CHANNEL = "blog:ws:user";

    /**
     * Redis 频道：WebSocket 广播通知。
     */
    public static final String BROADCAST_CHANNEL = "blog:ws:broadcast";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 向指定用户发布 WebSocket 通知（各实例本地推送）。
     *
     * @param userId       用户 ID
     * @param notification 通知内容
     */
    public void publishToUser(Long userId, NotificationDTO notification) {
        if (userId == null || notification == null) {
            return;
        }
        try {
            WebSocketNotifyMessage msg = new WebSocketNotifyMessage(userId, notification);
            stringRedisTemplate.convertAndSend(USER_CHANNEL, objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            log.error("序列化 WebSocket 通知失败: userId={}", userId, e);
        }
    }

    /**
     * 向所有在线用户发布广播通知。
     *
     * @param notification 通知内容
     */
    public void publishBroadcast(NotificationDTO notification) {
        if (notification == null) {
            return;
        }
        try {
            stringRedisTemplate.convertAndSend(BROADCAST_CHANNEL, objectMapper.writeValueAsString(notification));
        } catch (JsonProcessingException e) {
            log.error("序列化 WebSocket 广播失败", e);
        }
    }

    /**
     * WebSocket 定向通知消息体。
     *
     * @param userId       用户 ID
     * @param notification 通知内容
     */
    public record WebSocketNotifyMessage(Long userId, NotificationDTO notification) {
    }
}
