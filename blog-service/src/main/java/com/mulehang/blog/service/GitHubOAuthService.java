package com.mulehang.blog.service;

import com.mulehang.blog.dto.GitHubUserInfoDTO;
import com.mulehang.blog.vo.LoginResponse;

/**
 * GitHub OAuth 服务接口
 * 
 * @author mulehang
 * @date 2026-01-21
 */
public interface GitHubOAuthService {
    
    /**
     * 获取 GitHub OAuth 授权 URL
     * 
     * @param state 状态码（用于防止 CSRF 攻击）
     * @return 授权 URL
     */
    String getAuthorizeUrl(String state);
    
    /**
     * 通过授权码获取 GitHub Access Token
     * 
     * @param code 授权码
     * @return Access Token
     */
    String getAccessToken(String code);
    
    /**
     * 通过 Access Token 获取 GitHub 用户信息
     * 
     * @param accessToken Access Token
     * @return GitHub 用户信息
     */
    GitHubUserInfoDTO getUserInfo(String accessToken);
    
    /**
     * GitHub OAuth 登录
     * 
     * @param code 授权码
     * @return 登录响应（包含 JWT Token 和用户信息）
     */
    LoginResponse login(String code);
}
