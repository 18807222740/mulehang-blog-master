package com.mulehang.blog.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.model.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证入口点
 * 处理未认证请求，返回统一 JSON 格式错误响应
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        log.debug("未认证访问: {}", request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Result<?> result = Result.fail(ResultCodeEnum.UNAUTHORIZED, "未登录或登录已过期");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
