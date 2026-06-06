package com.mulehang.blog.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel AOP 配置
 * 注册 SentinelResourceAspect，使 @SentinelResource 注解生效
 */
@Configuration
public class SentinelAspectConfiguration {

    /**
     * 注册 Sentinel 资源切面
     * 用于处理 @SentinelResource 注解
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
