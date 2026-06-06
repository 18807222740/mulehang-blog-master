package com.mulehang.blog.service;

import com.mulehang.blog.dto.LoginRequest;
import com.mulehang.blog.dto.RegisterRequest;
import com.mulehang.blog.vo.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含 Token 和用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 登录响应（注册成功后自动登录）
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 退出登录（用于主动失效 Token）
     *
     * @param userId 用户ID
     * @param token  当前 Token
     */
    void logout(Long userId, String token);

}
