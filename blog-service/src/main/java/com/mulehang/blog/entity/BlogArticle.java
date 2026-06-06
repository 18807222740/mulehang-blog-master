package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文章
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("blog_article")
public class BlogArticle extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private String title;// 标题

    private String slug;// 文章唯一标识

    private String summary;// 摘要

    private String coverUrl;// 封面图片地址

    private Integer status;// 状态

    private Integer sourceType;// 来源类型

    private Integer allowComment;// 是否允许评论

    private Integer isPinned;// 是否置顶

    private Long authorId;// 逻辑外键，关联 sys_user.id

    private Long categoryId;// 逻辑外键，关联 blog_category.id

    private Long columnId;// 逻辑外键，关联 blog_column.id

    private Integer wordCount;// 字数

    private Long readCount;// 阅读量

    private Integer likeCount;// 点赞数

    private Integer commentCount;// 评论数

    private LocalDateTime publishTime;// 发布时间
}
