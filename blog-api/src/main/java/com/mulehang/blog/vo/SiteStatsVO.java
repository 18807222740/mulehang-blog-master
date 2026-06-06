package com.mulehang.blog.vo;

import lombok.Data;

/**
 * 网站统计数据 VO
 */
@Data
public class SiteStatsVO {

    /**
     * 今日 PV（页面浏览量）
     */
    private Long todayPV;

    /**
     * 今日 UV（独立访客数）
     */
    private Long todayUV;

    /**
     * 总 PV
     */
    private Long totalPV;

    /**
     * 总 UV
     */
    private Long totalUV;

    /**
     * 文章总数
     */
    private Long totalArticles;

    /**
     * 总阅读量
     */
    private Long totalReads;

    /**
     * 总点赞数
     */
    private Long totalLikes;

    /**
     * 总评论数
     */
    private Long totalComments;
}
