package com.mulehang.blog.es.document;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章 ES 文档模型（对应 Elasticsearch 索引中的一条文档）。
 *
 * <p>设计目标：</p>
 * <ul>
 *     <li>字段尽量“扁平化”，避免 ES 侧复杂 join / nested 结构，便于全文检索和列表展示。</li>
 *     <li>尽量做到“自包含”：搜索列表展示需要的字段尽量都放在 ES 文档里，减少回表查询。</li>
 * </ul>
 *
 * <p>字段与索引 mapping 的关系：</p>
 * <ul>
 *     <li>title/summary/content：text 类型，用于全文检索（会配置分词器与高亮）。</li>
 *     <li>slug/coverUrl/categoryName/authorName/tags：keyword 类型，用于过滤/聚合/展示。</li>
 *     <li>categoryId/authorId/status/readCount/likeCount/commentCount：数值类型，用于过滤/排序。</li>
 *     <li>publishTime/createTime：date 类型，用于排序与时间范围过滤。</li>
 * </ul>
 *
 * <p>注意：</p>
 * <ul>
 *     <li>ES 中的文档 _id 建议直接使用文章 ID（字符串），便于 CRUD。</li>
 *     <li>本类不使用 Spring Data Elasticsearch 注解（@Document/@Field），我们用的是官方 Java Client。</li>
 * </ul>
 */
@Data
public class ArticleDocument {

    private Long id;// 文章ID（同时建议作为 ES 文档 _id）

    private String title;// 标题（全文检索字段）

    private String summary;// 摘要（全文检索字段）

    private String content;// 正文（全文检索字段，通常存 markdown 原文或纯文本）

    private String slug;// 文章唯一标识（用于前台跳转）

    private String coverUrl;// 封面图片地址

    private Long categoryId;// 分类ID（用于过滤）

    private String categoryName;// 分类名称（用于展示/聚合）

    private List<String> tags;// 标签名称列表（keyword 数组，用于过滤）

    private Long authorId;// 作者ID（用于过滤）

    private String authorName;// 作者名称（用于展示/聚合）

    private Integer status;// 状态（例如：0=草稿，1=已发布）

    private LocalDateTime publishTime;// 发布时间

    private LocalDateTime createTime;// 创建时间

    private Long readCount;// 阅读量（用于展示/排序）

    private Integer likeCount;// 点赞数

    private Integer commentCount;// 评论数
}