package com.mulehang.blog.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 自动配置类
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {
}
