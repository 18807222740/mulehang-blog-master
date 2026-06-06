package com.mulehang.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章创建 DTO
 */
@Data
public class ArticleCreateDTO {
    private String title;// 标题

    private String slug;// 文章唯一标识

    private String summary;// 摘要

    private String coverUrl;// 封面图片地址

    private Integer status;// 状态，默认草稿

    private Integer sourceType;// 来源类型，默认原创

    private Integer allowComment;// 是否允许评论，默认允许

    private Integer isPinned;// 是否置顶，默认不置顶

    private Long categoryId;// 分类ID

    private Long columnId;// 栏目ID

    private List<Long> tagIds;// 标签ID列表

    private String contentMd;// 原文Markdown内容
}
