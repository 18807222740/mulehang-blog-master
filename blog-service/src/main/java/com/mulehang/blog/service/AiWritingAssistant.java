package com.mulehang.blog.service;

import com.mulehang.blog.ai.AiService;
import com.mulehang.blog.ai.AiServiceFactory;
import com.mulehang.blog.ai.model.AiMessage;
import com.mulehang.blog.ai.model.AiRequest;
import com.mulehang.blog.ai.model.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI写作助手服务
 * 提供文章大纲生成、续写、润色、翻译等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiWritingAssistant {

    private final AiServiceFactory aiServiceFactory;

    /**
     * 生成文章大纲
     *
     * @param topic 文章主题
     * @return 大纲列表
     */
    public List<String> generateOutline(String topic) {
        return generateOutline(topic, null, null, null, null);
    }

    /**
     * 生成文章大纲（支持 BYOK 覆盖）。
     *
     * @param topic    文章主题
     * @param provider AI 服务提供商
     * @param baseUrl  自定义 Base URL
     * @param apiKey   自带 API Key
     * @param model    自定义模型 ID
     * @return 大纲列表
     */
    public List<String> generateOutline(String topic, String provider, String baseUrl, String apiKey, String model) {
        log.info("生成文章大纲, 主题: {}", topic);

        AiService aiService = resolveAiService(provider, baseUrl, apiKey);
        String prompt = "请为主题\"" + topic + "\"生成一个详细的文章大纲，用换行分隔每个要点：";

        AiRequest request = baseRequestBuilder(model)
                .messages(List.of(new AiMessage("user", prompt)))
                .temperature(0.7)
                .maxTokens(1000)
                .build();

        AiResponse response = aiService.chat(request);

        // 按换行符分割，过滤空行
        List<String> outline = Arrays.stream(response.getContent().split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        log.info("大纲生成完成, 共 {} 个要点", outline.size());
        return outline;
    }

    /**
     * 续写文章（流式输出）
     *
     * @param existingContent 已有内容
     * @return 续写内容流
     */
    public Flux<String> continueWriting(String existingContent) {
        return continueWriting(existingContent, null, null, null, null);
    }

    /**
     * 续写文章（流式输出，支持 BYOK 覆盖）。
     *
     * @param existingContent 已有内容
     * @param provider        AI 服务提供商
     * @param baseUrl         自定义 Base URL
     * @param apiKey          自带 API Key
     * @param model           自定义模型 ID
     * @return 续写内容流
     */
    public Flux<String> continueWriting(String existingContent, String provider, String baseUrl, String apiKey, String model) {
        log.info("开始续写文章, 已有内容长度: {}", existingContent.length());

        AiService aiService = resolveAiService(provider, baseUrl, apiKey);
        String prompt = "请继续完成以下文章：\n\n" + existingContent;

        AiRequest request = baseRequestBuilder(model)
                .messages(List.of(new AiMessage("user", prompt)))
                .temperature(0.8)
                .maxTokens(2000)
                .stream(true)
                .build();

        return aiService.chatStream(request)
                .doOnComplete(() -> log.info("续写完成"))
                .doOnError(e -> log.error("续写失败", e));
    }

    /**
     * 润色文章
     *
     * @param content 原始内容
     * @return 润色后的内容
     */
    public String polish(String content) {
        return polish(content, null, null, null, null);
    }

    /**
     * 润色文章（支持 BYOK 覆盖）。
     *
     * @param content  原始内容
     * @param provider AI 服务提供商
     * @param baseUrl  自定义 Base URL
     * @param apiKey   自带 API Key
     * @param model    自定义模型 ID
     * @return 润色后的内容
     */
    public String polish(String content, String provider, String baseUrl, String apiKey, String model) {
        log.info("开始润色文章, 原文长度: {}", content.length());

        AiService aiService = resolveAiService(provider, baseUrl, apiKey);
        String prompt = "请对以下文章进行润色，提升文字质量，但保持原意：\n\n" + content;

        AiRequest request = baseRequestBuilder(model)
                .messages(List.of(new AiMessage("user", prompt)))
                .temperature(0.5)
                .maxTokens(content.length() * 2)
                .build();

        AiResponse response = aiService.chat(request);
        String polished = response.getContent();

        log.info("润色完成, 结果长度: {}", polished.length());
        return polished;
    }

    /**
     * 翻译文本
     *
     * @param content        待翻译内容
     * @param targetLanguage 目标语言
     * @return 翻译后的内容
     */
    public String translate(String content, String targetLanguage) {
        return translate(content, targetLanguage, null, null, null, null);
    }

    /**
     * 翻译文本（支持 BYOK 覆盖）。
     *
     * @param content        待翻译内容
     * @param targetLanguage 目标语言
     * @param provider       AI 服务提供商
     * @param baseUrl        自定义 Base URL
     * @param apiKey         自带 API Key
     * @param model          自定义模型 ID
     * @return 翻译后的内容
     */
    public String translate(String content, String targetLanguage, String provider, String baseUrl, String apiKey, String model) {
        log.info("开始翻译, 目标语言: {}, 原文长度: {}", targetLanguage, content.length());

        AiService aiService = resolveAiService(provider, baseUrl, apiKey);
        String prompt = String.format(
            "请将以下内容翻译成%s：\n\n%s",
            targetLanguage, content
        );

        AiRequest request = baseRequestBuilder(model)
                .messages(List.of(new AiMessage("user", prompt)))
                .temperature(0.3)
                .maxTokens(content.length() * 3)
                .build();

        AiResponse response = aiService.chat(request);
        String translated = response.getContent();

        log.info("翻译完成, 结果长度: {}", translated.length());
        return translated;
    }

    /**
     * 解析并返回 AI 服务实例（支持 BYOK 覆盖）。
     *
     * @param provider AI 服务提供商
     * @param baseUrl  自定义 Base URL
     * @param apiKey   自带 API Key
     * @return AI 服务
     */
    private AiService resolveAiService(String provider, String baseUrl, String apiKey) {
        if (StringUtils.hasText(provider)) {
            return aiServiceFactory.getService(provider, baseUrl, apiKey);
        }
        return aiServiceFactory.getDefaultService(baseUrl, apiKey);
    }

    /**
     * 创建基础请求构造器（注入模型 ID）。
     *
     * @param model 自定义模型 ID
     * @return 请求构造器
     */
    private AiRequest.AiRequestBuilder baseRequestBuilder(String model) {
        AiRequest.AiRequestBuilder builder = AiRequest.builder();
        String normalized = normalizeOptional(model);
        if (normalized != null) {
            builder.model(normalized);
        }
        return builder;
    }

    /**
     * 规范化可选字符串参数。
     *
     * @param value 原始值
     * @return 处理后的值
     */
    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
