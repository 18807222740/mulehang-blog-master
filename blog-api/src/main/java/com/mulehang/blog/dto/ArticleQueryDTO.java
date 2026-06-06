package com.mulehang.blog.dto;

import lombok.Data;

/**
 * 文章查询条件 DTO。
 */
@Data
public class ArticleQueryDTO {
    private Integer pageNo = 1;// 当前页码

    private Integer pageSize = 10;// 每页大小

    private Integer status;// 状态

    private Long categoryId;// 分类ID

    private Long columnId;// 专栏ID

    private Long tagId;// 标签ID

    private String keyword;// 关键词

    private Long authorId;// 作者ID

    private String sortBy;// 根据什么排序，例如publishTime等

    private String sortOrder;// 排序顺序，例如asc或desc
}
