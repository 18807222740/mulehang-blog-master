package com.mulehang.blog.service;

/**
 * 点赞服务（Redisson 分布式锁防重复点赞）。
 */
public interface LikeService {

    /**
     * 点赞文章。
     *
     * @param userId 用户 ID
     * @param articleId 文章 ID
     * @return true=点赞成功；false=已点赞或未获取到锁
     */
    boolean likeArticle(Long userId, Long articleId);

    /**
     * 查询用户是否已点赞某文章。
     *
     * @param userId 用户 ID
     * @param articleId 文章 ID
     * @return true=已点赞；false=未点赞
     */
    boolean hasLiked(Long userId, Long articleId);

    /**
     * 取消点赞文章。
     *
     * @param userId 用户 ID
     * @param articleId 文章 ID
     * @return true=取消成功；false=未点赞或未获取到锁
     */
    boolean unlikeArticle(Long userId, Long articleId);
}
