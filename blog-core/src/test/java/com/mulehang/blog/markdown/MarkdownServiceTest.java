package com.mulehang.blog.markdown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownServiceTest {

    private final MarkdownService markdownService = new MarkdownService();

    @Test
    void renderToHtml_shouldRenderBasicMarkdown() {
        String html = markdownService.renderToHtml("# Title\n\nhello");
        assertTrue(html.contains("<h1"));
        assertTrue(html.contains("Title"));
        assertTrue(html.contains("hello"));
    }

    @Test
    void renderToHtml_shouldSanitizeXss() {
        String html = markdownService.renderToHtml("<script>alert(1)</script>hi");
        assertFalse(html.toLowerCase().contains("<script"));
        assertTrue(html.contains("hi"));
    }
}
