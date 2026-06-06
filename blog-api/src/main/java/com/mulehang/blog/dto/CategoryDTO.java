package com.mulehang.blog.dto;

import lombok.Data;

/**
 * 分类 DTO
 */
@Data
public class CategoryDTO {

    private Long parentId;// 父分类ID

    private String name;// 分类名

    private String slug;// 分类唯一标识

    private String description;// 描述

    private Integer sort = 100;// 排序值

    private Integer status;// 状态
}
