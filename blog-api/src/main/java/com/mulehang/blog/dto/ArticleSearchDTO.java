package com.mulehang.blog.dto;

import lombok.Data;

/**
 * 文章搜索 DTO（Elasticsearch）。
 *
 * <p>用途：用于接收 {@code GET /api/v1/articles/search} 的查询参数。</p>
 *
 * <p>与 {@link ArticleQueryDTO} 的区别：</p>
 * <ul>
 *     <li>{@link ArticleQueryDTO}：主要用于 MySQL 的条件查询（like/eq 等）。</li>
 *     <li>本 DTO：主要用于 Elasticsearch 的全文检索（multi_match + 高亮）。</li>
 * </ul>
 */
@Data
public class ArticleSearchDTO {

    private Integer pageNo = 1;// 当前页码（从 1 开始）

    private Integer pageSize = 10;// 每页大小

    private String keyword;// 搜索关键词（会在 title/summary/content 上做全文检索）

    private Long categoryId;// 可选：分类ID过滤

    private Long authorId;// 可选：作者ID过滤

    private String tag;// 可选：标签名称过滤（与索引中的 tags 字段匹配）
}