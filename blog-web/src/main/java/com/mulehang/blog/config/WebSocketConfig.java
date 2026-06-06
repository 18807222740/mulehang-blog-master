package com.mulehang.blog.config;

import com.mulehang.blog.websocket.CommentNotificationHandler;
import com.mulehang.blog.websocket.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 * 启用 WebSocket 支持，注册 WebSocket 处理器
 *
 * @author mulehang
 * @date 2026-01-21
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final CommentNotificationHandler commentNotificationHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    /** WebSocket 允许的来源列表，通过配置注入 */
    @Value("${websocket.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * 注册 WebSocket 处理器
     * 配置连接端点和跨域设置
     *
     * @param registry WebSocket 处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册评论通知 WebSocket 端点
        registry.addHandler(commentNotificationHandler, "/ws/notifications")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins(allowedOrigins);
    }
}
