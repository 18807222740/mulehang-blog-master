package com.mulehang.blog.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulehang.blog.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * WebSocket 广播通知 Redis 监听器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketBroadcastNotifyListener implements MessageListener {

    private final CommentNotificationHandler commentNotificationHandler;
    private final ObjectMapper objectMapper;

    /**
     * 处理广播 WebSocket 通知。
     *
     * @param message 消息体
     * @param pattern 频道
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (message == null || message.getBody() == null) {
            return;
        }
        try {
            NotificationDTO notification = objectMapper.readValue(message.getBody(), NotificationDTO.class);
            commentNotificationHandler.broadcastLocal(notification);
        } catch (Exception e) {
            log.error("处理 WebSocket 广播通知失败", e);
        }
    }
}
