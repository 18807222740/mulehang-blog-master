package com.mulehang.blog.ai;

import com.mulehang.blog.ai.config.AiProperties;
import com.mulehang.blog.ai.provider.AnthropicProvider;
import com.mulehang.blog.ai.provider.OpenAiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 服务工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceFactory {
    
    private final AiProperties aiProperties;
    private final Map<String, AiService> providerCache = new ConcurrentHashMap<>();

    /**
     * 获取默认 AI 服务
     */
    public AiService getDefaultService() {
        String provider = aiProperties.getDefaultProvider();
        return getService(provider);
    }

    /**
     * 获取默认 AI 服务（支持 BYOK 覆盖）。
     *
     * @param baseUrl 自定义 Base URL
     * @param apiKey  自带 API Key
     * @return AI 服务
     */
    public AiService getDefaultService(String baseUrl, String apiKey) {
        String provider = aiProperties.getDefaultProvider();
        return getService(provider, baseUrl, apiKey);
    }

    /**
     * 获取指定的 AI 服务
     */
    public AiService getService(String providerName) {
        return providerCache.computeIfAbsent(providerName, this::createProvider);
    }

    /**
     * 获取指定的 AI 服务（支持 BYOK 覆盖）。
     *
     * @param providerName Provider 名称
     * @param baseUrl      自定义 Base URL
     * @param apiKey       自带 API Key
     * @return AI 服务
     */
    public AiService getService(String providerName, String baseUrl, String apiKey) {
        if (!StringUtils.hasText(baseUrl) && !StringUtils.hasText(apiKey)) {
            return getService(providerName);
        }
        return createProviderWithOverrides(providerName, baseUrl, apiKey);
    }

    /**
     * 从配置创建 Provider，并缓存。
     *
     * @param providerName Provider 名称
     * @return AI 服务
     */
    private AiService createProvider(String providerName) {
        AiProperties.ProviderConfig config = requireProviderConfig(providerName);
        log.info("创建提供商 '{}'，Base URL: {}", providerName, config.getBaseUrl());
        return createProvider(providerName, config);
    }

    /**
     * 创建 Provider（不缓存），用于 BYOK 覆盖。
     *
     * @param providerName Provider 名称
     * @param baseUrl      自定义 Base URL
     * @param apiKey       自带 API Key
     * @return AI 服务
     */
    private AiService createProviderWithOverrides(String providerName, String baseUrl, String apiKey) {
        AiProperties.ProviderConfig baseConfig = requireProviderConfig(providerName);
        AiProperties.ProviderConfig overrideConfig = new AiProperties.ProviderConfig();
        overrideConfig.setBaseUrl(StringUtils.hasText(baseUrl) ? sanitizeBaseUrl(baseUrl) : baseConfig.getBaseUrl());
        overrideConfig.setApiKey(StringUtils.hasText(apiKey) ? apiKey : null);
        overrideConfig.setModel(baseConfig.getModel());
        overrideConfig.setTimeout(baseConfig.getTimeout());
        return createProvider(providerName, overrideConfig);
    }

    /**
     * 创建 Provider 实例。
     *
     * @param providerName Provider 名称
     * @param config       Provider 配置
     * @return AI 服务
     */
    private AiService createProvider(String providerName, AiProperties.ProviderConfig config) {
        String name = providerName == null ? "" : providerName.toLowerCase();
        return switch (name) {
            case "openai" -> new OpenAiProvider(config);
            case "anthropic" -> new AnthropicProvider(config);
            default -> {
                log.warn("未知提供商 '{}', 视为 OpenAI-compatible", providerName);
                yield new OpenAiProvider(config);
            }
        };
    }

    /**
     * 获取指定 Provider 的配置。
     *
     * @param providerName Provider 名称
     * @return Provider 配置
     */
    private AiProperties.ProviderConfig requireProviderConfig(String providerName) {
        if (aiProperties.getProviders() == null || !aiProperties.getProviders().containsKey(providerName)) {
            log.error("未配置提供商 '{}'", providerName);
            throw new IllegalArgumentException("未配置提供商: " + providerName);
        }
        return aiProperties.getProviders().get(providerName);
    }

    /**
     * 校验并标准化 Base URL。
     *
     * @param baseUrl 原始 Base URL
     * @return 标准化后的 Base URL
     */
    private String sanitizeBaseUrl(String baseUrl) {
        String trimmed = baseUrl == null ? "" : baseUrl.trim();
        if (!StringUtils.hasText(trimmed)) {
            return null;
        }
        URI uri = URI.create(trimmed);
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("baseUrl 仅支持 http/https 协议");
        }
        return trimmed;
    }
}
