package com.mulehang.blog.ai;

import com.mulehang.blog.ai.model.AiRequest;
import com.mulehang.blog.ai.model.AiResponse;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 服务统一接口
 */
public interface AiService {
    /**
     * 同步对话
     */
    AiResponse chat(AiRequest request);

    /**
     * 流式对话
     */
    Flux<String> chatStream(AiRequest request);

    /**
     * 生成文章摘要
     */
    String generateSummary(String content, int maxLength);

    /**
     * 推荐标题
     */
    List<String> suggestTitles(String content, int count);

    /**
     * 推荐标签
     */
    List<String> suggestTags(String content, int count);
}
