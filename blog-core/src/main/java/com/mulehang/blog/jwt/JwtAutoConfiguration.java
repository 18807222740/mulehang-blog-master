package com.mulehang.blog.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 自动配置：用于启用 {@link JwtProperties} 的配置绑定，并生成 Spring Boot 配置元数据。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {
}
