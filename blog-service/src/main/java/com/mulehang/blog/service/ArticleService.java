package com.mulehang.blog.service;

import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.vo.ArticleDetailVO;
import com.mulehang.blog.vo.ArticleListVO;
import com.mulehang.blog.vo.ArticlePublicVO;

import java.util.List;

/**
 * 文章 Service。
 */
public interface ArticleService {

    /**
     * 创建文章。
     */
    Long createArticle(ArticleCreateDTO dto);

    /**
     * 更新文章。
     */
    void updateArticle(Long id, ArticleUpdateDTO dto);

    /**
     * 发布文章。
     */
    void publishArticle(Long id);

    /**
     * 获取文章详情。
     */
    ArticleDetailVO getArticleDetail(Long id);

    /**
     * 根据 slug 获取文章详情（后台/编辑用，包含 contentMd）。
     */
    ArticleDetailVO getArticleBySlug(String slug);

    /**
     * 根据 slug 获取前台文章详情（不含 contentMd）。
     * <p>
     * 用于前台展示，避免泄露原始 Markdown 内容。
     * </p>
     */
    ArticlePublicVO getPublicArticleBySlug(String slug);

    /**
     * 获取热门文章 TopN（按 Redis 热榜排序）。
     */
    List<ArticleListVO> listHotArticles(int topN);

    /**
     * 分页查询文章。
     */
    PageResult<ArticleListVO> listArticles(ArticleQueryDTO query);

    /**
     * 删除文章。
     */
    void deleteArticle(Long id);
}
