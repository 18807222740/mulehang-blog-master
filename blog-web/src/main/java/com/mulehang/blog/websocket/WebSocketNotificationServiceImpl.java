package com.mulehang.blog.websocket;

import com.mulehang.blog.dto.NotificationDTO;
import com.mulehang.blog.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WebSocket 通知服务（Redis Pub/Sub 多实例 + 本地推送）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final CommentNotificationHandler commentNotificationHandler;
    private final WebSocketNotifyBroadcaster webSocketNotifyBroadcaster;

    /**
     * 向指定用户发送通知。
     *
     * @param userId       用户 ID
     * @param notification 通知内容
     */
    @Override
    public void sendToUser(Long userId, NotificationDTO notification) {
        try {
            webSocketNotifyBroadcaster.publishToUser(userId, notification);
            log.debug("WebSocket 通知已发布: userId={}, type={}", userId, notification.getType());
        } catch (Exception e) {
            log.error("WebSocket 通知发布失败: userId={}", userId, e);
        }
    }

    /**
     * 广播通知。
     *
     * @param notification 通知内容
     */
    @Override
    public void broadcast(NotificationDTO notification) {
        try {
            webSocketNotifyBroadcaster.publishBroadcast(notification);
            log.debug("WebSocket 广播已发布: type={}", notification.getType());
        } catch (Exception e) {
            log.error("WebSocket 广播发布失败", e);
        }
    }

    /**
     * 获取在线用户数（本实例）。
     *
     * @return 在线用户数
     */
    @Override
    public int getOnlineUserCount() {
        return commentNotificationHandler.getOnlineUserCount();
    }

    /**
     * 检查用户是否在线（本实例）。
     *
     * @param userId 用户 ID
     * @return 是否在线
     */
    @Override
    public boolean isUserOnline(Long userId) {
        return commentNotificationHandler.isUserOnline(userId);
    }
}
