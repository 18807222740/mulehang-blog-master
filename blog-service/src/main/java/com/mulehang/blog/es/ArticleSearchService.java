package com.mulehang.blog.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.mulehang.blog.dto.ArticleSearchDTO;
import com.mulehang.blog.es.document.ArticleDocument;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.util.MarkdownRenderer;
import com.mulehang.blog.vo.ArticleSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文章搜索服务（Elasticsearch）。
 *
 * <p>
 * 职责：
 * </p>
 * <ul>
 * <li>构建 ES 查询 DSL：multi_match（title/summary/content） +
 * filter（status/category/author/tag）</li>
 * <li>分页：from/size</li>
 * <li>高亮：title/summary</li>
 * <li>将 ES 命中结果转换为统一分页返回 {@link PageResult}<{@link ArticleSearchVO}></li>
 * </ul>
 *
 * <p>
 * 容错说明：
 * </p>
 * <ul>
 * <li>若 ES 不可用，抛出 {@link IllegalStateException} 由上层统一处理。</li>
 * <li>ES 属于可选组件，因此本服务仅在容器中存在 {@link ElasticsearchClient} 时才会创建。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(ElasticsearchClient.class)
@SuppressWarnings("unused") // Spring Bean 由容器管理，静态分析可能误报“未使用”
public class ArticleSearchService {

    /**
     * 已发布状态（与 {@code ArticleServiceImpl} 保持一致）。
     * <p>
     * 虽然我们的索引同步逻辑已经“草稿不入 ES”，但这里仍做一层 status 过滤以增加安全性。
     * </p>
     */
    private static final int STATUS_PUBLISHED = 1;

    private final ElasticsearchClient esClient;
    private final MarkdownRenderer markdownRenderer;

    /**
     * 搜索文章。
     *
     * <p>
     * 查询策略（改进版）：
     * </p>
     * <ul>
     * <li>keyword 非空：使用 bool.should 组合多种匹配方式：
     * <ul>
     * <li>multi_match：全文检索（title^3, summary^2, content）</li>
     * <li>wildcard：支持前缀匹配（title*, summary*）</li>
     * </ul>
     * </li>
     * <li>keyword 为空：match_all（仅过滤条件生效）</li>
     * </ul>
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public PageResult<ArticleSearchVO> search(ArticleSearchDTO dto) {
        // 说明：lambda 表达式中使用的局部变量必须是 final / 有效 final，因此这里先把入参规整到 query 变量。
        final ArticleSearchDTO query = dto == null ? new ArticleSearchDTO() : dto;

        final int pageNo = query.getPageNo() == null || query.getPageNo() <= 0 ? 1 : query.getPageNo();
        final int rawPageSize = query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        // 防止一次拉太多导致 ES 压力过大（学习项目给个上限）
        final int pageSize = Math.min(rawPageSize, 50);

        final int from = (pageNo - 1) * pageSize;

        final String keyword = query.getKeyword();
        final String normalizedKeyword = keyword == null ? "" : keyword.trim();
        final boolean hasKeyword = !normalizedKeyword.isEmpty();
        final String loweredKeyword = normalizedKeyword.toLowerCase();

        final Long categoryId = query.getCategoryId();
        final Long authorId = query.getAuthorId();
        final String tag = query.getTag();
        final boolean hasTag = tag != null && !tag.isBlank();

        try {
            SearchResponse<ArticleDocument> response = esClient.search(s -> s
                    .index(EsIndexNames.BLOG_ARTICLE)
                    .from(from)
                    .size(pageSize)
                    // 需要 total 才能正确返回 PageResult.total
                    .trackTotalHits(t -> t.enabled(true))
                    // query：bool(must + filter)
                    .query(q -> q.bool(b -> {
                        // ===== 过滤：只搜已发布 =====
                        b.filter(f -> f.term(t -> t
                                .field("status")
                                .value(v -> v.longValue(STATUS_PUBLISHED))));

                        // ===== 过滤：分类/作者/标签 =====
                        if (categoryId != null) {
                            b.filter(f -> f.term(t -> t
                                    .field("categoryId")
                                    .value(v -> v.longValue(categoryId))));
                        }
                        if (authorId != null) {
                            b.filter(f -> f.term(t -> t
                                    .field("authorId")
                                    .value(v -> v.longValue(authorId))));
                        }
                        if (hasTag) {
                            b.filter(f -> f.term(t -> t
                                    .field("tags")
                                    .value(v -> v.stringValue(tag))));
                        }

                        // ===== 必须：关键词全文检索或 match_all =====
                        if (hasKeyword) {
                            // 改进：使用 bool.should 组合多种匹配方式
                            b.must(m -> m.bool(bb -> bb
                                    // 至少匹配一种
                                    .minimumShouldMatch("1")
                                    // 1. 全文检索（适合完整词汇）
                                    .should(sh -> sh.multiMatch(mm -> mm
                                            .query(normalizedKeyword)
                                            .fields("title^3", "summary^2", "content")
                                            .type(TextQueryType.BestFields)))
                                    // 2. 前缀匹配（适合输入中的部分字符）
                                    .should(sh -> sh.bool(pb -> pb
                                            .should(ps -> ps.wildcard(w -> w
                                                    .field("title")
                                                    .value(loweredKeyword + "*")
                                                    .boost(2.0f)))
                                            .should(ps -> ps.wildcard(w -> w
                                                    .field("summary")
                                                    .value(loweredKeyword + "*")
                                                    .boost(1.0f)))))));
                        } else {
                            b.must(m -> m.matchAll(ma -> ma));
                        }

                        return b;
                    }))
                    // 高亮：title/summary
                    .highlight(h -> h
                            .fields("title", f -> f.preTags("<em>").postTags("</em>"))
                            .fields("summary", f -> f.preTags("<em>").postTags("</em>"))),
                    ArticleDocument.class);

            return toPageResult(response, pageNo, pageSize);
        } catch (IOException e) {
            log.warn("ES 搜索失败: msg={}", e.getMessage());
            log.debug("ES 搜索异常详情", e);
            throw new IllegalStateException("Elasticsearch 搜索失败，请检查 ES 是否启动与 spring.elasticsearch.uris 配置", e);
        }
    }

    /**
     * 将 ES 响应转换为统一分页模型。
     *
     * @param response ES 搜索响应
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页大小
     * @return {@link PageResult}
     */
    private PageResult<ArticleSearchVO> toPageResult(SearchResponse<ArticleDocument> response, long pageNo,
            long pageSize) {
        List<Hit<ArticleDocument>> hits = response.hits().hits();
        List<ArticleSearchVO> list = new ArrayList<>(hits.size());

        for (Hit<ArticleDocument> hit : hits) {
            ArticleDocument doc = hit.source();
            if (doc == null) {
                continue;
            }

            ArticleSearchVO vo = new ArticleSearchVO();
            vo.setId(doc.getId());
            vo.setTitle(doc.getTitle());
            vo.setSummary(doc.getSummary());
            vo.setSlug(doc.getSlug());
            vo.setCoverUrl(doc.getCoverUrl());

            vo.setAuthorId(doc.getAuthorId());
            vo.setAuthorName(doc.getAuthorName());
            vo.setCategoryId(doc.getCategoryId());
            vo.setCategoryName(doc.getCategoryName());
            vo.setTags(doc.getTags());

            vo.setStatus(doc.getStatus());
            vo.setReadCount(doc.getReadCount());
            vo.setLikeCount(doc.getLikeCount());
            vo.setCommentCount(doc.getCommentCount());
            vo.setPublishTime(doc.getPublishTime());
            vo.setCreateTime(doc.getCreateTime());

            // 高亮字段：可能不存在
            Map<String, List<String>> highlight = hit.highlight();
            if (highlight != null) {
                vo.setHighlightTitle(cleanHighlight(joinHighlight(highlight.get("title"))));
                vo.setHighlightSummary(cleanHighlight(joinHighlight(highlight.get("summary"))));
            }

            list.add(vo);
        }

        var totalHits = response.hits().total();
        long total = totalHits == null ? list.size() : totalHits.value();

        PageResult<ArticleSearchVO> pr = new PageResult<>();
        pr.setList(list);
        pr.setTotal(total);
        pr.setPageNo(pageNo);
        pr.setPageSize(pageSize);
        return pr;
    }

    /**
     * 拼接高亮片段。
     *
     * <p>
     * ES 高亮可能返回多个片段；这里简单拼接成一个字符串返回。
     * </p>
     *
     * @param fragments 高亮片段列表
     * @return 拼接后的高亮字符串（片段为空则返回 null）
     */
    private String joinHighlight(List<String> fragments) {
        if (fragments == null || fragments.isEmpty()) {
            return null;
        }
        if (fragments.size() == 1) {
            return fragments.getFirst();
        }
        StringBuilder sb = new StringBuilder();
        for (String f : fragments) {
            if (f != null) {
                sb.append(f);
            }
        }
        return sb.toString();
    }

    /**
     * 清洗高亮 HTML，限制为安全白名单。
     *
     * <p>
     * 避免前端直接渲染高亮字段时出现 XSS。
     * </p>
     *
     * @param highlight 高亮字符串
     * @return 清洗后的高亮字符串
     */
    private String cleanHighlight(String highlight) {
        if (highlight == null || highlight.isBlank()) {
            return highlight;
        }
        return markdownRenderer.sanitizeHighlight(highlight);
    }
}
