package com.mulehang.blog.service;

import com.mulehang.blog.dto.NotificationDTO;

/**
 * WebSocket 通知服务接口
 * 负责管理 WebSocket 连接和消息推送
 *
 * @author mulehang
 * @date 2026-01-21
 */
public interface WebSocketNotificationService {

    /**
     * 向指定用户发送通知
     *
     * @param userId 用户ID
     * @param notification 通知消息
     */
    void sendToUser(Long userId, NotificationDTO notification);

    /**
     * 广播通知给所有在线用户
     *
     * @param notification 通知消息
     */
    void broadcast(NotificationDTO notification);

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数
     */
    int getOnlineUserCount();

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(Long userId);
}
