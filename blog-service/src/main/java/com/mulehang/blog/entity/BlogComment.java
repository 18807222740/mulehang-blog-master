package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章评论
 */
@EqualsAndHashCode(callSuper = true)
@TableName("blog_comment")
@Data
public class BlogComment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private Long articleId;// 文章ID

    private Long rootId;// 根评论ID,0表示自身为根

    private Long parentId;// 父评论ID,0表示直接评论文章

    private Long userId;// 逻辑外键，关联 sys_user.id

    private Long replyToUser;// 逻辑外键，被回复的用户ID，可为空

    private String content;// 评论内容

    private Integer status;// 评论状态，0表示待审核，1表示已通过，2表示已拒绝/屏蔽

    private Integer likeCount;// 点赞数

    private String ipAddress;// IP地址

    private String userAgent;// 用户代理信息

    private String location;// 地理位置信息

    private Integer isTop;// 是否置顶，0表示否，1表示是
}
