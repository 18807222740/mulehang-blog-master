package com.mulehang.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章搜索结果 VO（Elasticsearch）。
 *
 * <p>用途：</p>
 * <ul>
 *     <li>作为 {@code GET /api/v1/articles/search} 的返回列表元素类型。</li>
 *     <li>字段尽量“自包含”，避免为了展示搜索列表而再次回表查询。</li>
 * </ul>
 *
 * <p>关于高亮：</p>
 * <ul>
 *     <li>{@code highlightTitle}/{@code highlightSummary} 中一般会包含 {@code <em></em>} 等标签。</li>
 *     <li>若某个字段没有高亮结果（例如未命中），对应高亮字段可能为 {@code null}。</li>
 *     <li>前端展示时：优先使用高亮字段；高亮为空时回退到原始字段。</li>
 * </ul>
 */
@Data
public class ArticleSearchVO {

    private Long id;// 文章ID（与 MySQL 的 blog_article.id 一致，同时也是 ES 文档 _id）

    private String title;// 标题（原始标题，不带高亮标签）

    private String summary;// 摘要（原始摘要，不带高亮标签）

    private String slug;// 文章唯一标识（用于前台跳转）

    private String coverUrl;// 封面图片地址

    private Long authorId;// 作者 ID

    private String authorName;// 作者名称（用于列表展示，通常取昵称或用户名）

    private Long categoryId;// 分类 ID

    private String categoryName;// 分类名称（用于列表展示）

    private List<String> tags;// 标签名称列表（keyword 数组）

    private Integer status;// 文章状态（例如：0=草稿，1=已发布）

    private Long readCount;// 阅读量

    private Integer likeCount;// 点赞数

    private Integer commentCount;// 评论数

    private LocalDateTime publishTime;// 发布时间

    private LocalDateTime createTime;// 创建时间

    private String highlightTitle;// 标题高亮（可能包含 <em> 标签）

    private String highlightSummary;// 摘要高亮（可能包含 <em> 标签）
}