package com.mulehang.blog.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * WebSocket 定向通知 Redis 监听器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketUserNotifyListener implements MessageListener {

    private final CommentNotificationHandler commentNotificationHandler;
    private final ObjectMapper objectMapper;

    /**
     * 处理定向 WebSocket 通知。
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
            WebSocketNotifyBroadcaster.WebSocketNotifyMessage notifyMessage =
                    objectMapper.readValue(message.getBody(), WebSocketNotifyBroadcaster.WebSocketNotifyMessage.class);
            if (notifyMessage.userId() != null && notifyMessage.notification() != null) {
                commentNotificationHandler.sendToUserLocal(notifyMessage.userId(), notifyMessage.notification());
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 定向通知失败", e);
        }
    }
}
