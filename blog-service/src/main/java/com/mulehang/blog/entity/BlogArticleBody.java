package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章内容
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("blog_article_body")
public class BlogArticleBody extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private Long articleId;// 文章ID

    private String contentMd;// 原文

    private String contentHtml;// 渲染后的HTML内容
}
