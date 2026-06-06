package com.mulehang.blog.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * AI 配置属性
 */
@Data
@ConfigurationProperties(prefix = "blog.ai")
public class AiProperties {
    /**
     * 默认使用的 Provider (openai, anthropic)
     */
    private String defaultProvider = "openai";
    
    /**
     * 各个 Provider 的具体配置
     */
    private Map<String, ProviderConfig> providers;

    @Data
    public static class ProviderConfig {
        /**
         * API Key
         */
        private String apiKey;
        
        /**
         * Base URL
         */
        private String baseUrl;
        
        /**
         * 默认模型
         */
        private String model;
        
        /**
         * 超时时间（秒）
         */
        private Integer timeout = 60;
    }
}
