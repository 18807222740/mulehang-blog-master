package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章标签
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("blog_tag")
public class BlogTag extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private String name;// 标签名称

    private String slug;// 标签唯一标识

    private String color;// 展示颜色

    private String description;// 描述

    private Long creatorId;// 创建者ID，逻辑外键，关联 sys_user.id
}
