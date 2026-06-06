package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章标签关联
 */
@EqualsAndHashCode(callSuper = true)
@TableName("blog_article_tag")
@Data
public class BlogArticleTag extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private Long articleId;// 文章ID

    private Long tagId;// 标签ID
}
