package com.mulehang.blog.util;

import com.mulehang.blog.markdown.MarkdownService;
import org.springframework.stereotype.Component;

/**
 * Markdown 渲染器（Markdown -> HTML）。
 */
@Component
public class MarkdownRenderer {

    private final MarkdownService markdownService;

    public MarkdownRenderer(MarkdownService markdownService) {
        this.markdownService = markdownService;
    }

    /**
     * 将 Markdown 转换为 HTML。
     * @param markdown Markdown 文本
     * @return HTML 文本
     */
    public String renderToHtml(String markdown) {
        return markdownService.renderToHtml(markdown);
    }

    /**
     * 清洗高亮 HTML（仅允许 <em> 等必要标签）。
     *
     * @param highlight 高亮片段
     * @return 清洗后的高亮片段
     */
    public String sanitizeHighlight(String highlight) {
        return markdownService.sanitizeHighlight(highlight);
    }
}
