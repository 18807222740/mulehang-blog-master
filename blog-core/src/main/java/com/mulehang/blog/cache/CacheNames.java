package com.mulehang.blog.cache;

/**
 * Spring Cache 的缓存名称统一定义。
 */
public final class CacheNames {

    private CacheNames() {
    }

    public static final String ARTICLE_DETAIL = "article:detail";// 文章详情缓存
    public static final String CATEGORY_LIST = "category:list";// 分类列表缓存
    public static final String TAG_LIST = "tag:list";// 标签列表缓存
    public static final String HOT_ARTICLES = "hot:articles";// 热门文章榜缓存
}
