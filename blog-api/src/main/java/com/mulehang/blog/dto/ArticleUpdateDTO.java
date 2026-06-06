package com.mulehang.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章更新 DTO。
 * <p>
 * 说明：为了支持“部分更新”，这里字段不做必填约束；Service 层按非 null 字段进行更新。
 */
@Data
public class ArticleUpdateDTO {

    private String title;// 标题

    private String slug;// 文章唯一标识

    private String summary;// 摘要

    private String coverUrl;// 封面图片地址

    private Integer status;// 状态

    private Integer sourceType;// 来源类型

    private Integer allowComment;// 是否允许评论

    private Integer isPinned;// 是否置顶

    private Long categoryId;// 分类 ID

    private Long columnId;// 专栏 ID

    private List<Long> tagIds;// 标签 ID列表

    private String contentMd;// 原文 Markdown 内容
}
