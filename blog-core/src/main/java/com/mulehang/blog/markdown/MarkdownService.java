package com.mulehang.blog.markdown;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Markdown 渲染服务（Markdown -> HTML）。
 * <p>
 * 目标：
 * <ul>
 *     <li>支持常用 GFM 能力：表格、任务列表、删除线、自动链接等</li>
 *     <li>输出 HTML 做 XSS 清洗，防止脚本注入</li>
 * </ul>
 */
@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final Safelist safelist;

    public MarkdownService() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create(),
                TaskListExtension.create(),
                AutolinkExtension.create(),
                StrikethroughExtension.create(),
                TypographicExtension.create(),
                EmojiExtension.create()
        ));

        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();

        // XSS 防护：基于 jsoup Safelist 白名单过滤。
        // relaxed: 允许 a/img/blockquote/pre/code/table 等常用展示标签。
        this.safelist = Safelist.relaxed()
                // 允许代码块/高亮时的 class
                .addAttributes("code", "class")
                .addAttributes("pre", "class")
                .addAttributes("span", "class")
                .addTags("span")
                // table 相关属性
                .addAttributes("table", "class")
                .addAttributes("th", "align")
                .addAttributes("td", "align")
                // 链接协议限制
                .addProtocols("a", "href", "http", "https", "mailto")
                .preserveRelativeLinks(true);
    }

    /**
     * 将 Markdown 渲染为 HTML（并进行 XSS 清洗）。
     */
    public String renderToHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Document document = parser.parse(markdown);
        String html = renderer.render(document);
        return Jsoup.clean(html, safelist);
    }

    /**
     * 清洗高亮片段 HTML（仅允许有限标签与属性）。
     *
     * <p>用于 ES 高亮结果（通常包含 <em>），避免前端渲染时的 XSS。</p>
     *
     * @param highlight 高亮 HTML
     * @return 清洗后的高亮 HTML
     */
    public String sanitizeHighlight(String highlight) {
        if (highlight == null || highlight.isBlank()) {
            return highlight;
        }
        Safelist highlightSafelist = Safelist.none()
                .addTags("em")
                .addAttributes(":all", "class");
        return Jsoup.clean(highlight, highlightSafelist);
    }
}
