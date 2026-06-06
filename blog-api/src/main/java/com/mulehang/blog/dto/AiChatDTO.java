package com.mulehang.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * AI 聊天请求 DTO
 */
@Data
public class AiChatDTO {
    /**
     * 消息列表
     */
    @NotEmpty(message = "消息列表不能为空")
    private List<MessageDTO> messages;
    
    /**
     * 温度 (0-2)
     */
    private Double temperature;
    
    /**
     * 最大 Token
     */
    private Integer maxTokens;
    
    /**
     * 指定 Provider (可选，不指定则使用默认)
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

    @Data
    public static class MessageDTO {
        /**
         * 角色: system, user, assistant
         */
        private String role;
        
        /**
         * 内容
         */
        private String content;
    }
}
