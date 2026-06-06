package com.mulehang.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 助手请求 DTO
 */
@Data
public class AiAssistantDTO {
    /**
     * 文章内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;
    
    /**
     * 最大长度（用于摘要）
     */
    private Integer maxLength;
    
    /**
     * 生成数量（用于标题/标签）
     */
    private Integer count;

    /**
     * AI 服务提供商（可选）
     */
    private String provider;

    /**
     * 自定义 Base URL（BYOK）
     */
    @Size(max = 255, message = "Base URL 长度不能超过255位")
    private String baseUrl;

    /**
     * 自定义模型 ID
     */
    @Size(max = 100, message = "模型 ID 长度不能超过100位")
    private String model;

    /**
     * 自带 API Key（可选）
     */
    @Size(max = 200, message = "API Key 长度不能超过200位")
    private String apiKey;
}
