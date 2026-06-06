package com.mulehang.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 写作助手请求 DTO
 */
@Data
@Schema(description = "AI 写作助手请求")
public class AiWritingDTO {

    @Schema(description = "文章主题（用于生成大纲）", example = "Spring Boot 3.x 新特性详解")
    @Size(max = 200, message = "主题长度不能超过200字符")
    private String topic;

    @Schema(description = "文章内容（用于续写/润色/翻译）", example = "Spring Boot 3.0带来了许多重大更新...")
    @Size(max = 50000, message = "内容长度不能超过50000字符")
    private String content;

    @Schema(description = "目标语言（用于翻译）", example = "英文")
    @Size(max = 50, message = "目标语言长度不能超过50字符")
    private String targetLanguage;

    @Schema(description = "目标字数（用于扩写/缩写）", example = "1000")
    @Min(value = 100, message = "目标字数不能小于100")
    @Max(value = 10000, message = "目标字数不能超过10000")
    private Integer targetLength;

    @Schema(description = "AI服务提供商（openai/anthropic，可选，为空则使用默认）", example = "openai")
    private String provider;

    @Schema(description = "自定义 Base URL（BYOK）", example = "https://api.openai.com")
    @Size(max = 255, message = "Base URL 长度不能超过255字符")
    private String baseUrl;

    @Schema(description = "自定义模型 ID", example = "gpt-4o-mini")
    @Size(max = 100, message = "模型 ID 长度不能超过100字符")
    private String model;

    @Schema(description = "自带 API Key（可选）", example = "sk-xxx")
    @Size(max = 200, message = "API Key 长度不能超过200字符")
    private String apiKey;
}
