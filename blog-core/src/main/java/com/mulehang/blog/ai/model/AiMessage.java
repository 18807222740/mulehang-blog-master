package com.mulehang.blog.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {
    /**
     * 角色：system, user, assistant
     */
    private String role;
    
    /**
     * 内容
     */
    private String content;
}
