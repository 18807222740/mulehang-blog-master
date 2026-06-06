package com.mulehang.blog.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {
    /**
     * 响应内容
     */
    private String content;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 消耗的 Token 数（可选）
     */
    private Integer usage;
}
