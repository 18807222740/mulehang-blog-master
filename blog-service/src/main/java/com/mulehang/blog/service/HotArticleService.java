package com.mulehang.blog.service;

import java.util.List;

/**
 * 热门文章榜服务（Redis ZSet）。
 * <p>
 * member = articleId，score = 热度（可按阅读量/点击量累加）。
 */
public interface HotArticleService {

    /**
     * 阅读量 +1（ZINCRBY）。
     */
    void incrementReadCount(Long articleId);

    /**
     * 获取 TopN 热门文章 ID（ZREVRANGE）。
     */
    List<Long> getHotArticleIds(int topN);

    /**
     * 清空热榜（例如每日 0 点重置）。
     */
    void resetHotArticles();
}
