package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章专栏/系列
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("blog_column")
public class BlogColumn extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private String name;// 专栏名称

    private String slug;// 专栏唯一标识

    private String coverUrl;// 封面图

    private String description;// 描述

    private Integer sort;// 排序值

    private Integer status;// 状态
}
