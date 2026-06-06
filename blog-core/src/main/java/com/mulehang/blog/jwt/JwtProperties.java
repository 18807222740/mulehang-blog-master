package com.mulehang.blog.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置。
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 密钥（建议至少 32 字符）。
     */
    private String secret;

    /**
     * access token 过期时间（毫秒）。
     */
    private Long expiration;

    /**
     * refresh token 过期时间（毫秒）。
     */
    private Long refreshExpiration;
}
