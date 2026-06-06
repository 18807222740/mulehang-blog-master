package com.mulehang.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表 VO
 */
@Data
public class ArticleListVO {

    private Long id;// 文章ID

    private String title;// 标题

    private String slug;// 文章唯一标识

    private String summary;// 摘要

    private String coverUrl;// 封面图片地址

    private Integer status;// 状态

    private UserVO author;// 作者

    private CategoryVO category;// 分类

    private List<TagVO> tags;// 标签

    private Long readCount;// 阅读量

    private Integer likeCount;// 点赞数

    private Integer commentCount;// 评论数

    private LocalDateTime publishTime;// 发布时间

    private LocalDateTime createTime;// 创建时间

    private LocalDateTime updateTime;// 更新时间
}
