package com.mulehang.blog.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulehang.blog.ai.AiService;
import com.mulehang.blog.ai.config.AiProperties;
import com.mulehang.blog.ai.model.AiMessage;
import com.mulehang.blog.ai.model.AiRequest;
import com.mulehang.blog.ai.model.AiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OpenAI 协议兼容实现
 */
@Slf4j
public class OpenAiProvider implements AiService {

    private final AiProperties.ProviderConfig config;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiProvider(AiProperties.ProviderConfig config) {
        this.config = config;
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()
                        ? config.getBaseUrl()
                        : "https://api.openai.com");
        if (StringUtils.hasText(config.getApiKey())) {
            builder.defaultHeader("Authorization", "Bearer " + config.getApiKey());
        }
        this.webClient = builder.build();
    }

    @Override
    public AiResponse chat(AiRequest request) {
        Map<String, Object> body = buildRequestBody(request, false);

        Map<String, Object> response = webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(errorBody -> new RuntimeException(
                                "AI 请求失败: status=" + clientResponse.statusCode()
                                        + ", body=" + errorBody)))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .block();

        return parseResponse(response);
    }

    @Override
    public Flux<String> chatStream(AiRequest request) {
        Map<String, Object> body = buildRequestBody(request, true);

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        return new String(bytes, StandardCharsets.UTF_8);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .flatMap(this::parseStreamChunk)
                .doOnError(error -> log.error("流式响应处理错误", error));
    }

    @Override
    public String generateSummary(String content, int maxLength) {
        String prompt = String.format("请为以下文章生成一段不超过%d字的摘要：\n\n%s", maxLength, content);
        AiRequest request = AiRequest.builder()
                .messages(List.of(new AiMessage("user", prompt)))
                .maxTokens(Math.max(maxLength * 2, 500))
                .build();
        return chat(request).getContent();
    }

    @Override
    public List<String> suggestTitles(String content, int count) {
        String prompt = String.format("请为以下文章推荐%d个具有吸引力的标题，每行一个，不要包含序号或引导语：\n\n%s", count, content);
        AiRequest request = AiRequest.builder()
                .messages(List.of(new AiMessage("user", prompt)))
                .build();
        String result = chat(request).getContent();
        return List.of(result.split("\n")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> suggestTags(String content, int count) {
        String prompt = String.format("请为以下文章提取%d个最相关的标签，用英文逗号分隔，不要包含引导语：\n\n%s", count, content);
        AiRequest request = AiRequest.builder()
                .messages(List.of(new AiMessage("user", prompt)))
                .build();
        String result = chat(request).getContent();
        return List.of(result.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildRequestBody(AiRequest request, boolean stream) {
        Map<String, Object> body = new HashMap<>();
        String model = resolveModel(request.getModel());
        body.put("model", model);
        body.put("messages", request.getMessages());
        body.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);
        body.put("stream", stream);
        Integer normalizedMaxTokens = normalizeMaxTokens(request.getMaxTokens(), model);
        if (normalizedMaxTokens != null) {
            body.put("max_tokens", normalizedMaxTokens);
        }
        return body;
    }

    /**
     * 规范化 max_tokens，兼容部分 OpenAI-compatible 的上限。
     * DeepSeek 最大 8192。
     */
    private Integer normalizeMaxTokens(Integer maxTokens, String model) {
        if (maxTokens == null) {
            return null;
        }
        int normalized = Math.max(1, maxTokens);
        String baseUrl = config.getBaseUrl();
        boolean isDeepSeek = StringUtils.hasText(baseUrl) && baseUrl.contains("deepseek.com");
        if (isDeepSeek && normalized > 8192) {
            log.warn("检测到 DeepSeek Base URL，max_tokens {} 超过上限 8192，已自动截断。model={}", normalized, model);
            return 8192;
        }
        return normalized;
    }

    /**
     * 解析并适配模型名称。
     * 对 DeepSeek OpenAI-compatible Base URL 做默认模型兜底。
     *
     * @param requestedModel 请求中的模型
     * @return 最终模型
     */
    private String resolveModel(String requestedModel) {
        String model = StringUtils.hasText(requestedModel) ? requestedModel : config.getModel();
        String baseUrl = config.getBaseUrl();
        if (StringUtils.hasText(baseUrl)
                && baseUrl.contains("deepseek.com")
                && (!StringUtils.hasText(model) || model.startsWith("gpt-"))) {
            log.warn("检测到 DeepSeek Base URL，自动使用模型 deepseek-chat（原模型: {}）", model);
            return "deepseek-chat";
        }
        return model;
    }

    private AiResponse parseResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("choices")) {
            throw new RuntimeException("Invalid response from AI provider: choices not found");
        }
        Object choicesObject = response.get("choices");
        if (!(choicesObject instanceof List<?> choices) || choices.isEmpty()) {
            throw new RuntimeException("Invalid response from AI provider: choices is empty");
        }
        Object firstChoiceObject = choices.get(0);
        if (!(firstChoiceObject instanceof Map<?, ?> firstChoice)) {
            throw new RuntimeException("Invalid response from AI provider: choice format error");
        }
        String content = extractContentFromChoice(firstChoice);
        if (!StringUtils.hasText(content)) {
            log.warn("AI 返回内容为空，model={}, rawChoiceKeys={}", response.get("model"), firstChoice.keySet());
        }

        Integer totalTokens = null;
        Object usageObject = response.get("usage");
        if (usageObject instanceof Map<?, ?> usage) {
            Object totalTokensObject = usage.get("total_tokens");
            if (totalTokensObject instanceof Number number) {
                totalTokens = number.intValue();
            }
        }
        Object modelObject = response.get("model");

        return AiResponse.builder()
                .content(content)
                .model(modelObject == null ? null : modelObject.toString())
                .usage(totalTokens)
                .build();
    }

    /**
     * 从不同协议的响应中提取内容。
     * 兼容 OpenAI-compatible 与部分模型的 reasoning 字段。
     *
     * @param choice choices[0]
     * @return 内容文本
     */
    private String extractContentFromChoice(Map<?, ?> choice) {
        if (choice == null) {
            return "";
        }
        Object messageObject = choice.get("message");
        if (messageObject instanceof Map<?, ?> message) {
            Object contentObject = message.get("content");
            String content = contentObject == null ? "" : contentObject.toString();
            if (StringUtils.hasText(content)) {
                return content;
            }
            Object reasoningObject = message.get("reasoning_content");
            String reasoning = reasoningObject == null ? "" : reasoningObject.toString();
            if (StringUtils.hasText(reasoning)) {
                return reasoning;
            }
        }
        Object textObject = choice.get("text");
        return textObject == null ? "" : textObject.toString();
    }

    /**
     * 解析SSE流式响应数据块
     * OpenAI SSE格式: data: {json}\n\n
     * 响应结构: {"choices": [{"delta": {"content": "文本"}}]}
     *
     * @param chunk SSE数据块
     * @return 提取的文本内容流
     */
    private Flux<String> parseStreamChunk(String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return Flux.empty();
        }

        List<String> results = new ArrayList<>();
        String[] lines = chunk.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("data: ")) {
                String data = line.substring(6).trim();

                // 跳过[DONE]标记
                if ("[DONE]".equals(data)) {
                    continue;
                }

                try {
                    JsonNode node = objectMapper.readTree(data);
                    JsonNode choices = node.get("choices");

                    if (choices != null && choices.isArray() && choices.size() > 0) {
                        JsonNode delta = choices.get(0).get("delta");
                        if (delta != null && delta.has("content")) {
                            JsonNode contentNode = delta.get("content");
                            if (contentNode != null && !contentNode.isNull()) {
                                results.add(contentNode.asText());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("解析SSE数据块失败,跳过: {}", line, e);
                }
            }
        }

        return Flux.fromIterable(results);
    }
}
