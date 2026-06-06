package com.mulehang.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * GitHub OAuth 回调 DTO
 * 
 * @author mulehang
 * @date 2026-01-21
 */
@Data
public class GitHubOAuthCallbackDTO {
    
    /**
     * GitHub 返回的授权码
     */
    @NotBlank(message = "授权码不能为空")
    private String code;

    /**
     * 状态码（用于防止 CSRF 攻击）
     */
    @NotBlank(message = "state 不能为空")
    private String state;
}
