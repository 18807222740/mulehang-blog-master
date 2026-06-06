package com.mulehang.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论 VO
 */
@Data
public class CommentVO {
    private Long id;// 评论 ID

    private Long articleId;// 文章 ID

    private String articleTitle;// 文章标题

    private String articleSlug;// 文章 slug

    private Long rootId;// 根评论 ID

    private Long parentId;// 父评论 ID

    private Long userId;// 用户 ID

    private String username;// 用户名

    private String nickname;// 用户昵称

    private String avatar;// 用户头像

    private Long replyToUser;// 被回复的用户 ID

    private String content;// 评论内容

    private Integer likeCount;// 点赞数

    private Boolean liked;// 当前用户是否已点赞

    private Integer status;// 评论状态，0表示待审核，1表示已通过，2表示已拒绝/屏蔽

    private String location;// IP 归属地

    private Integer isTop;// 是否置顶，0表示否，1表示是

    private LocalDateTime createTime;// 创建时间
}
