package com.mulehang.blog.vo;

import lombok.Data;

/**
 * 分类 VO
 */
@Data
public class CategoryVO {

    private Long id;// 分类ID

    private Long parentId;// 父分类ID

    private String name;// 分类名

    private String slug;// 分类唯一标识

    private String description;// 描述

    private Integer sort;// 排序值

    private Integer status;// 状态

    private Integer articleCount;// 文章数量

    private Long creatorId;// 创建者ID
}
