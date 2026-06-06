package com.mulehang.blog.service;

/**
 * 缓存一致性辅助服务。
 */
public interface CacheConsistencyService {

    /**
     * 清除文章详情缓存（立即 + 延迟双删内部处理）。
     */
    void evictArticleDetail(Long articleId);
}
