package com.mulehang.blog.dto;

import lombok.Data;

/**
 * 栏目 DTO
 */
@Data
public class ColumnDTO {

    private String name;// 栏目名

    private String slug;// 栏目唯一标识

    private String coverUrl;// 封面图片地址

    private String description;// 描述

    private Integer sort = 100;// 排序值

    private Integer status;// 状态
}
