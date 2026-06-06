package com.mulehang.blog.vo;

import lombok.Data;

/**
 * 专栏 VO
 */
@Data
public class ColumnVO {

    private Long id;// 专栏ID

    private String name;// 专栏名

    private String slug;// 专栏唯一标识

    private String coverUrl;// 封面图片地址

    private String description;// 描述

    private Integer sort;// 排序值

    private Integer status;// 状态
}
