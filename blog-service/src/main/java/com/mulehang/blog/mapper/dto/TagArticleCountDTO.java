package com.mulehang.blog.mapper.dto;

import lombok.Data;

/**
 * 标签文章数量 DTO。
 */
@Data
public class TagArticleCountDTO {

    private Long tagId;// 标签 ID

    private Long articleCount;// 文章数量
}
