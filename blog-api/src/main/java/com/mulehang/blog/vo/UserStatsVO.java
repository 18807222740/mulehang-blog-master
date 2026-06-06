package com.mulehang.blog.vo;

import lombok.Data;

/**
 * 用户统计信息。
 */
@Data
public class UserStatsVO {

    private Long articleCount;// 发表文章数

    private Long commentCount;// 发表评论数

    private Long likeCount;// 获得点赞数
}
