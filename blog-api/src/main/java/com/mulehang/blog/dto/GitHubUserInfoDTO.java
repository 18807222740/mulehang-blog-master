package com.mulehang.blog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitHub 用户信息 DTO
 * 
 * @author mulehang
 * @date 2026-01-21
 */
@Data
public class GitHubUserInfoDTO {
    
    /**
     * GitHub 用户 ID
     */
    private Long id;
    
    /**
     * GitHub 用户名
     */
    private String login;
    
    /**
     * 用户昵称
     */
    private String name;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像 URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 博客地址
     */
    private String blog;
    
    /**
     * 公司
     */
    private String company;
    
    /**
     * 地址
     */
    private String location;
}
