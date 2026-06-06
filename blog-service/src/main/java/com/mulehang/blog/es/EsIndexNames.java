package com.mulehang.blog.es;

/**
 * Elasticsearch 索引名常量。
 *
 * <p>目的：避免在代码里到处硬编码索引名，后续需要改动时只改一处即可。</p>
 */
public final class EsIndexNames {

    /**
     * 文章搜索索引。
     */
    public static final String BLOG_ARTICLE = "blog_article";

    /**
     * 私有构造器：工具类不允许实例化。
     */
    private EsIndexNames() {
    }
}