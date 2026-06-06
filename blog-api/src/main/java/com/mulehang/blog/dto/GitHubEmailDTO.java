package com.mulehang.blog.dto;

import lombok.Data;

/**
 * GitHub 邮箱信息 DTO
 * 
 * @author mulehang
 * @date 2026-01-21
 */
@Data
public class GitHubEmailDTO {
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 是否为主邮箱
     */
    private boolean primary;
    
    /**
     * 是否已验证
     */
    private boolean verified;
    
    /**
     * 可见性
     */
    private String visibility;
}
