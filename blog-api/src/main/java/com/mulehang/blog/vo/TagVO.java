package com.mulehang.blog.vo;

import lombok.Data;

/**
 * 标签 VO
 */
@Data
public class TagVO {

    private Long id;// 标签ID

    private String name;// 标签名

    private String slug;// 标签唯一标识

    private String color;// 展示颜色

    private String description;// 描述

    private Long creatorId;// 创建者ID

    private Long articleCount;// 文章数量
}
