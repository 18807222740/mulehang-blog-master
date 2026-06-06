package com.mulehang.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 前台文章详情 VO（不含 contentMd）。
 * <p>
 * 用于前台通过 slug 获取文章详情，不返回原始 Markdown 内容，
 * 避免"直接抄走原稿"与带宽浪费。
 * </p>
 */
@Data
public class ArticlePublicVO {
    /** 文章 ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 文章唯一标识 */
    private String slug;

    /** 摘要 */
    private String summary;

    /** 封面图片地址 */
    private String coverUrl;

    /** 状态 */
    private Integer status;

    /** 来源类型 */
    private Integer sourceType;

    /** 是否允许评论 */
    private Integer allowComment;

    /** 是否置顶 */
    private Integer isPinned;

    /** 作者 */
    private UserVO author;

    /** 分类 */
    private CategoryVO category;

    /** 栏目 */
    private ColumnVO column;

    /** 标签 */
    private List<TagVO> tags;

    /** 字数 */
    private Integer wordCount;

    /** 阅读量 */
    private Long readCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 渲染后的 HTML 内容（不含原始 Markdown） */
    private String contentHtml;
}
