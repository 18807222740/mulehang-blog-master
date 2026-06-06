package com.mulehang.blog.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.dto.GitHubOAuthCallbackDTO;
import com.mulehang.blog.dto.LoginRequest;
import com.mulehang.blog.dto.RegisterRequest;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.AuthService;
import com.mulehang.blog.service.GitHubOAuthService;
import com.mulehang.blog.service.OAuthStateService;
import com.mulehang.blog.vo.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、注册、退出等接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String AUTH_COOKIE_NAME = "AUTH_TOKEN";

    private final AuthService authService;
    private final GitHubOAuthService gitHubOAuthService;
    private final OAuthStateService oAuthStateService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token")
    @PostMapping("/login")
    @SentinelResource(value = "auth-login", blockHandler = "loginBlockHandler")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request);
        writeAuthCookie(httpRequest, httpResponse, response.getToken(), response.getExpiresIn());
        return Result.ok(response);
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 登录响应
     */
    @Operation(summary = "用户注册", description = "注册新用户，注册成功后自动登录")
    @PostMapping("/register")
    @SentinelResource(value = "auth-register", blockHandler = "registerBlockHandler")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                          HttpServletRequest httpRequest,
                                          HttpServletResponse httpResponse) {
        LoginResponse response = authService.register(request);
        writeAuthCookie(httpRequest, httpResponse, response.getToken(), response.getExpiresIn());
        return Result.ok(response);
    }

    /**
     * 退出登录

     * @return 成功消息
     */
    @Operation(summary = "退出登录", description = "退出当前登录状态")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Long userId = UserContext.getCurrentUserId();
        String token = extractToken(httpRequest);
        authService.logout(userId, token);
        clearAuthCookie(httpRequest, httpResponse);
        return Result.ok();
    }

    /**
     * 获取 GitHub OAuth 授权 URL
     *
     * @return GitHub 授权 URL
     */
    @Operation(summary = "GitHub 授权", description = "获取 GitHub OAuth 授权 URL，用于跳转到 GitHub 登录页面")
    @GetMapping("/oauth/github/authorize")
    public Result<String> githubAuthorize(@RequestParam(required = false) String state) {
        // 如果没有传递 state，生成随机 state
        if (state == null || state.isBlank()) {
            state = UUID.randomUUID().toString().replace("-", "");
        }
        oAuthStateService.storeState(state);
        String authorizeUrl = gitHubOAuthService.getAuthorizeUrl(state);
        return Result.ok(authorizeUrl);
    }

    /**
     * GitHub OAuth 回调接口
     *
     * @param callback 回调参数（包含 code 和 state）
     * @return 登录响应（包含 JWT Token 和用户信息）
     */
    @Operation(summary = "GitHub OAuth 回调", description = "GitHub 授权回调接口，用于接收 GitHub 返回的授权码")
    @GetMapping("/oauth/github/callback")
    public Result<LoginResponse> githubCallback(@Valid GitHubOAuthCallbackDTO callback,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        if (!oAuthStateService.consumeState(callback.getState())) {
            return Result.fail("OAuth state 校验失败，请重新登录");
        }
        LoginResponse response = gitHubOAuthService.login(callback.getCode());
        writeAuthCookie(httpRequest, httpResponse, response.getToken(), response.getExpiresIn());
        return Result.ok(response);
    }

    /**
     * 登录限流降级处理。
     */
    public Result<LoginResponse> loginBlockHandler(LoginRequest request,
                                                   HttpServletRequest httpRequest,
                                                   HttpServletResponse httpResponse,
                                                   BlockException e) {
        return Result.fail("登录过于频繁，请稍后再试");
    }

    /**
     * 注册限流降级处理。
     */
    public Result<LoginResponse> registerBlockHandler(RegisterRequest request,
                                                      HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse,
                                                      BlockException e) {
        return Result.fail("注册过于频繁，请稍后再试");
    }

    /**
     * 写入认证 Cookie。
     */
    private void writeAuthCookie(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String token,
                                 Long expiresInSeconds) {
        if (token == null || token.isBlank()) {
            return;
        }
        long maxAge = expiresInSeconds == null || expiresInSeconds <= 0 ? 86400 : expiresInSeconds;
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite("Lax")
                .path("/")
                .maxAge(java.time.Duration.ofSeconds(maxAge))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 清理认证 Cookie。
     */
    private void clearAuthCookie(HttpServletRequest request, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 判断请求是否为 HTTPS。
     */
    private boolean isSecureRequest(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null) {
            return "https".equalsIgnoreCase(forwardedProto);
        }
        return request.isSecure();
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
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
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
