package com.mulehang.blog.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 请求模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 消息列表
     */
    private List<AiMessage> messages;
    
    /**
     * 温度
     */
    private Double temperature;
    
    /**
     * 最大 Token 数
     */
    private Integer maxTokens;
    
    /**
     * 是否流式输出
     */
    private Boolean stream;
}
