package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章分类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("blog_category")
public class BlogCategory extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private Long parentId;// 逻辑外键，关联 blog_category.id，0 表示顶级

    private String name;// 分类名称

    private String slug;// 分类唯一标识

    private String description;// 描述

    private Integer sort;// 排序值

    private Integer status;// 状态

    private Long creatorId;// 创建者ID，逻辑外键，关联 sys_user.id
}
