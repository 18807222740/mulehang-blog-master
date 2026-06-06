package com.mulehang.blog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GitHub Access Token 响应 DTO
 * 
 * @author mulehang
 * @date 2026-01-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubAccessTokenDTO {

    /**
     * 访问令牌
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 令牌类型
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * 授权范围
     */
    private String scope;

    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty("error_uri")
    private String errorUri;
}
