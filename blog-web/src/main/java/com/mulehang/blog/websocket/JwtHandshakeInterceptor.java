package com.mulehang.blog.websocket;

import com.mulehang.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * WebSocket 握手拦截器
 * 用于从 URL 参数中提取 JWT Token 并验证用户信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final String AUTH_COOKIE_NAME = "AUTH_TOKEN";

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = extractToken(httpRequest);
            if (token != null && !token.isEmpty()) {
                try {
                    // 验证 token 并提取用户 ID
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    if (userId != null) {
                        // 将用户 ID 存入 WebSocket 会话属性中
                        attributes.put("userId", userId);
                        log.debug("WebSocket 握手成功: userId={}", userId);
                        return true;
                    }
                } catch (Exception e) {
                    log.error("WebSocket 握手失败: Token 验证异常", e);
                }
            } else {
                log.warn("WebSocket 握手失败: 未找到 token");
            }
        }
        return false; // 验证失败，拒绝握手
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理
    }

    /**
     * 从请求头或 Cookie 提取 Token。
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (Objects.equals(AUTH_COOKIE_NAME, cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
            }
        }
        return null;
    }
}
