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
import java.util.stream.Stream;

/**
 * Anthropic 协议实现
 */
@Slf4j
public class AnthropicProvider implements AiService {

    private final AiProperties.ProviderConfig config;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicProvider(AiProperties.ProviderConfig config) {
        this.config = config;
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(config.getBaseUrl() != null && !config.getBaseUrl().isEmpty() 
                        ? config.getBaseUrl() : "https://api.anthropic.com")
                .defaultHeader("anthropic-version", "2023-06-01");
        if (StringUtils.hasText(config.getApiKey())) {
            builder.defaultHeader("x-api-key", config.getApiKey());
        }
        this.webClient = builder.build();
    }

    /**
     * 向 Anthropic API 发起非流式对话请求
     * @param request AI 请求对象
     * @return AI 响应结果
     */
    @Override
    public AiResponse chat(AiRequest request) {
        Map<String, Object> body = buildRequestBody(request, false);

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(config.getTimeout()))
                    .block();

            return parseResponse(response);
        } catch (Exception e) {
            log.error("Anthropic chat error", e);
            return AiResponse.builder().content("Error: " + e.getMessage()).build();
        }
    }

    @Override
    public Flux<String> chatStream(AiRequest request) {
        Map<String, Object> body = buildRequestBody(request, true);

        return webClient.post()
                .uri("/v1/messages")
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
        return Stream.of(result.split("\n"))
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
        return Stream.of(result.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 构建 Anthropic API 请求体
     *
     * @param request AI 请求对象
     * @param stream 是否为流式请求
     * @return 请求体 Map
     */
    private Map<String, Object> buildRequestBody(AiRequest request, boolean stream) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel() != null ? request.getModel() : config.getModel());
        
        // Anthropic 的 messages 必须交替，且第一个必须是 user
        // 这里简单处理，过滤掉 system 并转换
        List<Map<String, String>> messages = request.getMessages().stream()
                .filter(m -> !"system".equalsIgnoreCase(m.getRole()))
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());
        body.put("messages", messages);
        
        // 如果有 system 消息，放在顶层 system 参数中
        request.getMessages().stream()
                .filter(m -> "system".equalsIgnoreCase(m.getRole()))
                .findFirst()
                .ifPresent(m -> body.put("system", m.getContent()));

        body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 1024);
        body.put("stream", stream);
        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        return body;
    }

    /**
     * 解析 Anthropic API 响应
     *
     * @param response API 响应数据
     * @return AI 响应对象
     */
    private AiResponse parseResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("content")) {
            return AiResponse.builder().content("Error: Invalid response from Anthropic").build();
        }
        Object contentObject = response.get("content");
        if (!(contentObject instanceof List<?> contents) || contents.isEmpty()) {
            return AiResponse.builder().content("Error: No content in response").build();
        }
        
        StringBuilder sb = new StringBuilder();
        for (Object item : contents) {
            if (item instanceof Map<?, ?> content) {
                Object type = content.get("type");
                if ("text".equals(type)) {
                    Object text = content.get("text");
                    if (text != null) {
                        sb.append(text);
                    }
                }
            }
        }
        
        Integer totalTokens = null;
        Object usageObject = response.get("usage");
        if (usageObject instanceof Map<?, ?> usage) {
            Object outputTokens = usage.get("output_tokens");
            if (outputTokens instanceof Number number) {
                totalTokens = number.intValue();
            }
        }
        Object modelObject = response.get("model");

        return AiResponse.builder()
                .content(sb.toString())
                .model(modelObject == null ? null : modelObject.toString())
                .usage(totalTokens)
                .build();
    }

    /**
     * 解析SSE流式响应数据块
     * Anthropic SSE格式: data: {json}\n\n
     * 关键事件类型:
     * - message_start: 消息开始
     * - content_block_start: 内容块开始
     * - content_block_delta: 增量内容(包含实际文本)
     * - content_block_stop: 内容块结束
     * - message_stop: 消息结束
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
                    JsonNode typeNode = node.get("type");
                    
                    if (typeNode == null) {
                        continue;
                    }
                    
                    String type = typeNode.asText();
                    
                    // 提取文本增量
                    if ("content_block_delta".equals(type)) {
                        JsonNode delta = node.get("delta");
                        if (delta != null) {
                            JsonNode deltaType = delta.get("type");
                            if (deltaType != null && "text_delta".equals(deltaType.asText())) {
                                JsonNode textNode = delta.get("text");
                                if (textNode != null) {
                                    results.add(textNode.asText());
                                }
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
