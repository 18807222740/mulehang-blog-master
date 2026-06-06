package com.mulehang.blog.controller.api.v1;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mulehang.blog.ai.AiService;
import com.mulehang.blog.ai.AiServiceFactory;
import com.mulehang.blog.ai.model.AiMessage;
import com.mulehang.blog.ai.model.AiRequest;
import com.mulehang.blog.ai.model.AiResponse;
import com.mulehang.blog.dto.AiAssistantDTO;
import com.mulehang.blog.dto.AiChatDTO;
import com.mulehang.blog.dto.AiWritingDTO;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.AiWritingAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI 服务", description = "AI 对话与内容生产")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AiController {

        private final AiServiceFactory aiServiceFactory;
        private final AiWritingAssistant aiWritingAssistant;

        /**
         * 同步对话
         */
        @PostMapping("/chat")
        @Operation(summary = "AI 对话（同步）")
        @SentinelResource(value = "ai-chat", blockHandler = "chatBlockHandler", fallback = "chatFallback")
        public Result<String> chat(@Valid @RequestBody AiChatDTO dto) {
                AiService aiService = resolveAiService(dto.getProvider(), dto.getBaseUrl(), dto.getApiKey());

                List<AiMessage> messages = dto.getMessages().stream()
                                .map(m -> new AiMessage(m.getRole(), m.getContent()))
                                .collect(Collectors.toList());

                AiRequest request = AiRequest.builder()
                                .messages(messages)
                                .temperature(dto.getTemperature())
                                .maxTokens(dto.getMaxTokens())
                                .model(normalizeOptional(dto.getModel()))
                                .build();

                AiResponse response = aiService.chat(request);
                return Result.ok(response.getContent());
        }

        /**
         * 同步对话限流处理
         */
        public Result<String> chatBlockHandler(@Valid @RequestBody AiChatDTO dto, BlockException e) {
                log.warn("AI 对话接口被限流: {}", e.getRule());
                return Result.fail("请求过于频繁，请稍后再试");
        }

        /**
         * 同步对话降级处理
         */
        public Result<String> chatFallback(@Valid @RequestBody AiChatDTO dto, Throwable e) {
                log.error("AI 对话接口异常降级", e);
                return Result.fail(resolveAiErrorMessage(e));
        }

        /**
         * 流式对话(SSE)
         */
        @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        @Operation(summary = "AI 对话(流式)")
        @SentinelResource(value = "ai-chat-stream", blockHandler = "chatStreamBlockHandler", fallback = "chatStreamFallback")
        public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody AiChatDTO dto) {
                AiService aiService = resolveAiService(dto.getProvider(), dto.getBaseUrl(), dto.getApiKey());

                List<AiMessage> messages = dto.getMessages().stream()
                                .map(m -> new AiMessage(m.getRole(), m.getContent()))
                                .collect(Collectors.toList());

                AiRequest request = AiRequest.builder()
                                .messages(messages)
                                .temperature(dto.getTemperature())
                                .maxTokens(dto.getMaxTokens())
                                .model(normalizeOptional(dto.getModel()))
                                .stream(true)
                                .build();

                return aiService.chatStream(request)
                                .map(chunk -> ServerSentEvent.<String>builder()
                                                .data(chunk)
                                                .build())
                                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                                                .event("done")
                                                .data("[DONE]")
                                                .build()))
                                .onErrorResume(e -> {
                                        log.error("Stream error", e);
                                        return Flux.just(ServerSentEvent.<String>builder()
                                                        .event("error")
                                                        .data("[ERROR] " + e.getMessage())
                                                        .build());
                                });
        }

        /**
         * 流式对话限流处理
         */
        public Flux<ServerSentEvent<String>> chatStreamBlockHandler(@Valid @RequestBody AiChatDTO dto,
                        BlockException e) {
                log.warn("AI 流式对话接口被限流: {}", e.getRule());
                return Flux.just(ServerSentEvent.<String>builder()
                                .event("blocked")
                                .data("[BLOCKED] 请求过于频繁，请稍后再试")
                                .build());
        }

        /**
         * 流式对话降级处理
         */
        public Flux<ServerSentEvent<String>> chatStreamFallback(@Valid @RequestBody AiChatDTO dto, Throwable e) {
                log.error("AI 流式对话接口异常降级", e);
                String message = resolveAiErrorMessage(e);
                return Flux.just(ServerSentEvent.<String>builder()
                                .event("error")
                                .data("[ERROR] " + message)
                                .build());
        }

        /**
         * 生成文章摘要
         */
        @PostMapping("/assistant/summary")
        @Operation(summary = "生成文章摘要")
        @SentinelResource(value = "ai-assistant", blockHandler = "assistantBlockHandler", fallback = "assistantFallback")
        public Result<String> generateSummary(@Valid @RequestBody AiAssistantDTO dto) {
                AiService aiService = resolveAiService(dto.getProvider(), dto.getBaseUrl(), dto.getApiKey());
                int maxLength = dto.getMaxLength() != null ? dto.getMaxLength() : 200;
                String prompt = String.format("请为以下文章生成一段不超过%d字的摘要：\n\n%s", maxLength, dto.getContent());
                AiRequest request = AiRequest.builder()
                                .messages(List.of(new AiMessage("user", prompt)))
                                .maxTokens(Math.max(maxLength * 2, 500))
                                .model(normalizeOptional(dto.getModel()))
                                .build();
                AiResponse response = aiService.chat(request);
                return Result.ok(response.getContent());
        }

        /**
         * 推荐文章标题
         */
        @PostMapping("/assistant/titles")
        @Operation(summary = "推荐文章标题")
        @SentinelResource(value = "ai-assistant", blockHandler = "assistantBlockHandler", fallback = "assistantFallback")
        public Result<List<String>> suggestTitles(@Valid @RequestBody AiAssistantDTO dto) {
                AiService aiService = resolveAiService(dto.getProvider(), dto.getBaseUrl(), dto.getApiKey());
                int count = dto.getCount() != null ? dto.getCount() : 3;
                String prompt = String.format(
                                "请为以下文章推荐%d个具有吸引力的标题，每行一个，不要包含序号或引导语：\n\n%s",
                                count, dto.getContent());
                AiRequest request = AiRequest.builder()
                                .messages(List.of(new AiMessage("user", prompt)))
                                .model(normalizeOptional(dto.getModel()))
                                .build();
                AiResponse response = aiService.chat(request);
                List<String> titles = Arrays.stream(response.getContent().split("\n"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                return Result.ok(titles);
        }

        /**
         * 推荐文章标签
         */
        @PostMapping("/assistant/tags")
        @Operation(summary = "推荐文章标签")
        @SentinelResource(value = "ai-assistant", blockHandler = "assistantBlockHandler", fallback = "assistantFallback")
        public Result<List<String>> suggestTags(@Valid @RequestBody AiAssistantDTO dto) {
                AiService aiService = resolveAiService(dto.getProvider(), dto.getBaseUrl(), dto.getApiKey());
                int count = dto.getCount() != null ? dto.getCount() : 5;
                String prompt = String.format(
                                "请为以下文章提取%d个最相关的标签，用英文逗号分隔，不要包含引导语：\n\n%s",
                                count, dto.getContent());
                AiRequest request = AiRequest.builder()
                                .messages(List.of(new AiMessage("user", prompt)))
                                .model(normalizeOptional(dto.getModel()))
                                .build();
                AiResponse response = aiService.chat(request);
                List<String> tags = Arrays.stream(response.getContent().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                return Result.ok(tags);
        }

        /**
         * AI 助手功能限流处理（通用）
         */
        public Result<?> assistantBlockHandler(@Valid @RequestBody AiAssistantDTO dto, BlockException e) {
                log.warn("AI 助手接口被限流: {}", e.getRule());
                return Result.fail("请求过于频繁，请稍后再试");
        }

        /**
         * AI 助手功能降级处理（通用）
         */
        public Result<?> assistantFallback(@Valid @RequestBody AiAssistantDTO dto, Throwable e) {
                log.error("AI 助手接口异常降级", e);
                return Result.fail(resolveAiErrorMessage(e));
        }

        /**
         * 生成文章大纲
         */
        @PostMapping("/writing/outline")
        @Operation(summary = "生成文章大纲", description = "根据主题生成详细的文章大纲，返回列表形式的要点")
        @SentinelResource(value = "ai-writing", blockHandler = "writingBlockHandler", fallback = "writingFallback")
        public Result<List<String>> generateOutline(@Valid @RequestBody AiWritingDTO dto) {
                if (dto.getTopic() == null || dto.getTopic().trim().isEmpty()) {
                        return Result.fail("主题不能为空");
                }
                List<String> outline = aiWritingAssistant.generateOutline(
                                dto.getTopic(),
                                dto.getProvider(),
                                dto.getBaseUrl(),
                                dto.getApiKey(),
                                dto.getModel());
                return Result.ok(outline);
        }

        /**
         * 续写文章（同步）
         */
        @PostMapping("/writing/continue")
        @Operation(summary = "续写文章（同步）", description = "根据已有内容续写文章，返回完整结果（非流式）")
        @SentinelResource(value = "ai-writing", blockHandler = "writingBlockHandler", fallback = "writingFallback")
        public Result<String> continueWriting(@Valid @RequestBody AiWritingDTO dto) {
                if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                        return Result.fail("内容不能为空");
                }
                // 同步版本：收集完整结果
                String result = aiWritingAssistant.continueWriting(
                                dto.getContent(),
                                dto.getProvider(),
                                dto.getBaseUrl(),
                                dto.getApiKey(),
                                dto.getModel())
                                .collectList()
                                .map(chunks -> String.join("", chunks))
                                .block();
                return Result.ok(result);
        }

        /**
         * 续写文章（流式SSE）
         */
        @PostMapping(value = "/writing/continue/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        @Operation(summary = "续写文章（流式）", description = "根据已有内容续写文章，使用SSE流式输出结果，需要登录认证")
        @SentinelResource(value = "ai-writing-stream", blockHandler = "writingStreamBlockHandler", fallback = "writingStreamFallback")
        public Flux<ServerSentEvent<String>> continueWritingStream(@Valid @RequestBody AiWritingDTO dto) {
                if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                        return Flux.just(ServerSentEvent.<String>builder()
                                        .event("error")
                                        .data("[ERROR] 内容不能为空")
                                        .build());
                }

                return aiWritingAssistant.continueWriting(
                                dto.getContent(),
                                dto.getProvider(),
                                dto.getBaseUrl(),
                                dto.getApiKey(),
                                dto.getModel())
                                .map(chunk -> ServerSentEvent.<String>builder()
                                                .data(chunk)
                                                .build())
                                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                                                .event("done")
                                                .data("[DONE]")
                                                .build()))
                                .onErrorResume(e -> {
                                        log.error("续写流式输出失败", e);
                                        return Flux.just(ServerSentEvent.<String>builder()
                                                        .event("error")
                                                        .data("[ERROR] " + e.getMessage())
                                                        .build());
                                });
        }

        /**
         * 润色文章
         */
        @PostMapping("/writing/polish")
        @Operation(summary = "润色文章", description = "对文章内容进行润色优化，提升文字质量但保持原意")
        @SentinelResource(value = "ai-writing", blockHandler = "writingBlockHandler", fallback = "writingFallback")
        public Result<String> polish(@Valid @RequestBody AiWritingDTO dto) {
                if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                        return Result.fail("内容不能为空");
                }
                String polished = aiWritingAssistant.polish(
                                dto.getContent(),
                                dto.getProvider(),
                                dto.getBaseUrl(),
                                dto.getApiKey(),
                                dto.getModel());
                return Result.ok(polished);
        }

        /**
         * 翻译文章
         */
        @PostMapping("/writing/translate")
        @Operation(summary = "翻译文章", description = "将文章内容翻译成指定语言")
        @SentinelResource(value = "ai-writing", blockHandler = "writingBlockHandler", fallback = "writingFallback")
        public Result<String> translate(@Valid @RequestBody AiWritingDTO dto) {
                if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                        return Result.fail("内容不能为空");
                }
                if (dto.getTargetLanguage() == null || dto.getTargetLanguage().trim().isEmpty()) {
                        return Result.fail("目标语言不能为空");
                }
                String translated = aiWritingAssistant.translate(
                                dto.getContent(),
                                dto.getTargetLanguage(),
                                dto.getProvider(),
                                dto.getBaseUrl(),
                                dto.getApiKey(),
                                dto.getModel());
                return Result.ok(translated);
        }

        /**
         * 写作助手功能限流处理（通用）
         */
        public Result<?> writingBlockHandler(@Valid @RequestBody AiWritingDTO dto, BlockException e) {
                log.warn("AI 写作助手接口被限流: {}", e.getRule());
                return Result.fail("请求过于频繁，请稍后再试");
        }

        /**
         * 写作助手功能降级处理（通用）
         */
        public Result<?> writingFallback(@Valid @RequestBody AiWritingDTO dto, Throwable e) {
                log.error("AI 写作助手接口异常降级", e);
                return Result.fail(resolveAiErrorMessage(e));
        }

        /**
         * 写作助手流式接口限流处理
         */
        public Flux<ServerSentEvent<String>> writingStreamBlockHandler(@Valid @RequestBody AiWritingDTO dto,
                        BlockException e) {
                log.warn("AI 写作助手流式接口被限流: {}", e.getRule());
                return Flux.just(ServerSentEvent.<String>builder()
                                .event("blocked")
                                .data("[BLOCKED] 请求过于频繁，请稍后再试")
                                .build());
        }

        /**
         * 写作助手流式接口降级处理
         */
        public Flux<ServerSentEvent<String>> writingStreamFallback(@Valid @RequestBody AiWritingDTO dto, Throwable e) {
                log.error("AI 写作助手流式接口异常降级", e);
                String message = resolveAiErrorMessage(e);
                return Flux.just(ServerSentEvent.<String>builder()
                                .event("error")
                                .data("[ERROR] " + message)
                                .build());
        }

        /**
         * 解析 AI 请求异常，返回更易理解的提示语。
         *
         * @param e 异常信息
         * @return 面向用户的错误提示
         */
        private String resolveAiErrorMessage(Throwable e) {
                if (e == null || !StringUtils.hasText(e.getMessage())) {
                        return "AI 服务暂时不可用，请稍后再试";
                }
                String message = e.getMessage();
                String lower = message.toLowerCase();
                if (lower.contains("model not exist") || lower.contains("model_not_exist")
                                || lower.contains("model_not_found")) {
                        return "模型 ID 不存在，请检查模型配置（AI 工作台可修改）";
                }
                if (lower.contains("invalid_request_error") && lower.contains("model")) {
                        return "模型 ID 无效，请检查模型配置（AI 工作台可修改）";
                }
                if (lower.contains("invalid_api_key") || lower.contains("api key") || lower.contains("unauthorized")
                                || lower.contains("authentication")) {
                        return "API Key 无效或未配置，请检查密钥设置";
                }
                if (lower.contains("rate limit") || lower.contains("quota")) {
                        return "请求过于频繁或额度不足，请稍后再试";
                }
                return "AI 服务暂时不可用，请稍后再试";
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
