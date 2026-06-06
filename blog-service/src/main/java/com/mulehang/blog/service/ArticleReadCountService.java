package com.mulehang.blog.service;

/**
 * 文章阅读量统计服务（Redis 增量 + 定时刷盘 DB）。
 */
public interface ArticleReadCountService {

    /**
     * 记录一次阅读（Redis 增量 + 热榜累加，不直接写 DB）。
     *
     * @param articleId 文章 ID
     */
    void recordRead(Long articleId);

    /**
     * 计算展示用阅读量（DB 基值 + Redis 待刷盘增量）。
     *
     * @param articleId   文章 ID
     * @param dbReadCount 数据库中的阅读量
     * @return 展示阅读量
     */
    long resolveDisplayCount(Long articleId, Long dbReadCount);

    /**
     * 将 Redis 中待刷盘的阅读量增量批量写入数据库。
     */
    void flushPendingToDatabase();
}
