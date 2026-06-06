package com.mulehang.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章 VO（前台展示用）。
 * <p>
 * 注意：前台接口建议返回 {@code contentHtml}，不返回 {@code contentMd}。
 */
@Data
public class ArticleVO {

    private Long id;// 文章ID

    private String title;// 标题

    private String slug;// 文章唯一标识

    private String summary;// 摘要

    private String coverUrl;// 封面图片地址

    private Integer status;// 状态

    private Integer sourceType;// 来源类型

    private Integer allowComment;// 是否允许评论

    private Integer isPinned;// 是否置顶

    private UserVO author;// 作者

    private CategoryVO category;// 分类

    private ColumnVO column;// 栏目

    private List<TagVO> tags;// 标签

    private Integer wordCount;// 字数

    private Integer readCount;// 阅读量

    private Integer likeCount;// 点赞数

    private Integer commentCount;// 评论数

    private LocalDateTime publishTime;// 发布时间

    private String contentHtml;// 渲染后的HTML内容
}
