package com.mulehang.blog.dto;

import lombok.Data;

/**
 * 标签 DTO
 */
@Data
public class TagDTO {
    private String name;// 标签名

    private String slug;// 标签唯一标识

    private String color;// 展示颜色

    private String description;// 描述
}
