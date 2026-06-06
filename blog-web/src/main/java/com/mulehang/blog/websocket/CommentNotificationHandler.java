package com.mulehang.blog.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulehang.blog.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 评论通知 WebSocket 处理器
 * 处理 WebSocket 连接建立、关闭和消息推送
 *
 * @author mulehang
 * @date 2026-01-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationHandler extends TextWebSocketHandler {

    /**
     * 用户ID -> WebSocket会话 映射表
     * 用于快速查找用户的连接会话
     */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * JSON 序列化工具
     */
    private final ObjectMapper objectMapper;

    /**
     * WebSocket 连接建立后调用
     * 从会话中提取用户ID并建立映射关系
     *
     * @param session WebSocket 会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("WebSocket 连接建立成功: userId={}, sessionId={}", userId, session.getId());
        } else {
            log.warn("WebSocket 连接建立失败: 无法获取用户ID, sessionId={}", session.getId());
        }
    }

    /**
     * WebSocket 连接关闭后调用
     * 清理会话映射关系
     *
     * @param session WebSocket 会话
     * @param status 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket 连接关闭: userId={}, sessionId={}, status={}", 
                    userId, session.getId(), status);
        }
    }

    /**
     * 向指定用户发送通知消息（本地连接）。
     *
     * @param userId       用户 ID
     * @param notification 通知消息
     */
    public void sendToUserLocal(Long userId, NotificationDTO notification) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(json));
                log.debug("发送 WebSocket 消息成功: userId={}, type={}", userId, notification.getType());
            } catch (IOException e) {
                log.error("发送 WebSocket 消息失败: userId={}", userId, e);
            }
        } else {
            log.debug("用户不在线，无法发送 WebSocket 消息: userId={}", userId);
        }
    }

    /**
     * 广播消息给所有在线用户（本地连接）。
     *
     * @param notification 通知消息
     */
    public void broadcastLocal(NotificationDTO notification) {
        try {
            String json = objectMapper.writeValueAsString(notification);
            TextMessage message = new TextMessage(json);
            
            sessions.values().parallelStream()
                    .filter(WebSocketSession::isOpen)
                    .forEach(session -> {
                        try {
                            session.sendMessage(message);
                        } catch (IOException e) {
                            log.error("广播消息失败: sessionId={}", session.getId(), e);
                        }
                    });
            
            log.info("广播 WebSocket 消息成功: type={}, 在线用户数={}", 
                    notification.getType(), sessions.size());
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数
     */
    public int getOnlineUserCount() {
        return sessions.size();
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 从 WebSocket 会话中提取用户ID
     * 用户ID 已由 JwtHandshakeInterceptor 存入 session 属性中
     *
     * @param session WebSocket 会话
     * @return 用户ID，如果无法提取则返回 null
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }
}
