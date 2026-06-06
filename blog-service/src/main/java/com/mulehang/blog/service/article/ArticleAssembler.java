package com.mulehang.blog.service.article;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.entity.*;
import com.mulehang.blog.mapper.*;
import com.mulehang.blog.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章 VO 组装器（批量加载关联数据，避免 N+1 查询）。
 */
@Component
@RequiredArgsConstructor
public class ArticleAssembler {

    private final BlogArticleBodyMapper bodyMapper;
    private final SysUserMapper userMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogColumnMapper columnMapper;
    private final BlogTagMapper tagMapper;
    private final BlogArticleTagMapper articleTagMapper;

    /**
     * 组装单篇文章详情 VO。
     *
     * @param article 文章实体
     * @return 详情 VO
     */
    public ArticleDetailVO buildDetail(BlogArticle article) {
        if (article == null) {
            return null;
        }
        List<ArticleDetailVO> list = buildDetailBatch(List.of(article));
        return list.isEmpty() ? null : list.getFirst();
    }

    /**
     * 批量组装文章详情 VO。
     *
     * @param articles 文章列表
     * @return 详情 VO 列表
     */
    public List<ArticleDetailVO> buildDetailBatch(List<BlogArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> articleIds = articles.stream().map(BlogArticle::getId).filter(Objects::nonNull).toList();
        Map<Long, BlogArticleBody> bodyMap = loadBodies(articleIds);
        Map<Long, List<TagVO>> tagMap = loadTagsByArticleIds(articleIds);

        Set<Long> authorIds = articles.stream().map(BlogArticle::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream().map(BlogArticle::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> columnIds = articles.stream().map(BlogArticle::getColumnId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, SysUser> userMap = loadUsers(authorIds);
        Map<Long, BlogCategory> categoryMap = loadCategories(categoryIds);
        Map<Long, BlogColumn> columnMap = loadColumns(columnIds);

        List<ArticleDetailVO> result = new ArrayList<>(articles.size());
        for (BlogArticle article : articles) {
            ArticleDetailVO vo = new ArticleDetailVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSlug(article.getSlug());
            vo.setSummary(article.getSummary());
            vo.setCoverUrl(article.getCoverUrl());
            vo.setStatus(article.getStatus());
            vo.setSourceType(article.getSourceType());
            vo.setAllowComment(article.getAllowComment());
            vo.setIsPinned(article.getIsPinned());
            vo.setWordCount(article.getWordCount());
            vo.setReadCount(article.getReadCount());
            vo.setLikeCount(article.getLikeCount());
            vo.setCommentCount(article.getCommentCount());
            vo.setPublishTime(article.getPublishTime());
            vo.setCreateTime(article.getCreateTime());
            vo.setUpdateTime(article.getUpdateTime());

            BlogArticleBody body = bodyMap.get(article.getId());
            if (body != null) {
                vo.setContentMd(body.getContentMd());
                vo.setContentHtml(body.getContentHtml());
            } else {
                vo.setContentMd("");
                vo.setContentHtml("");
            }

            vo.setAuthor(toUserVO(userMap.get(article.getAuthorId())));
            vo.setCategory(toCategoryVO(article.getCategoryId() == null ? null : categoryMap.get(article.getCategoryId())));
            vo.setColumn(toColumnVO(article.getColumnId() == null ? null : columnMap.get(article.getColumnId())));
            vo.setTags(tagMap.getOrDefault(article.getId(), Collections.emptyList()));
            result.add(vo);
        }
        return result;
    }

    /**
     * 组装文章列表 VO。
     *
     * @param articles 文章实体列表
     * @return 列表 VO
     */
    public List<ArticleListVO> buildListVO(List<BlogArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> authorIds = articles.stream().map(BlogArticle::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream().map(BlogArticle::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Long> articleIds = articles.stream().map(BlogArticle::getId).toList();

        Map<Long, SysUser> userMap = loadUsers(authorIds);
        Map<Long, BlogCategory> categoryMap = loadCategories(categoryIds);
        Map<Long, List<TagVO>> tagMap = loadTagsByArticleIds(articleIds);

        List<ArticleListVO> list = new ArrayList<>(articles.size());
        for (BlogArticle a : articles) {
            ArticleListVO vo = new ArticleListVO();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setSlug(a.getSlug());
            vo.setSummary(a.getSummary());
            vo.setCoverUrl(a.getCoverUrl());
            vo.setStatus(a.getStatus());
            vo.setReadCount(a.getReadCount());
            vo.setLikeCount(a.getLikeCount());
            vo.setCommentCount(a.getCommentCount());
            vo.setPublishTime(a.getPublishTime());
            vo.setCreateTime(a.getCreateTime());
            vo.setUpdateTime(a.getUpdateTime());
            vo.setAuthor(toUserVO(userMap.get(a.getAuthorId())));
            vo.setCategory(toCategoryVO(a.getCategoryId() == null ? null : categoryMap.get(a.getCategoryId())));
            vo.setTags(tagMap.getOrDefault(a.getId(), Collections.emptyList()));
            list.add(vo);
        }
        return list;
    }

    /**
     * 将 ArticleDetailVO 转为前台公开 VO（不含 Markdown 原文）。
     *
     * @param detail 详情 VO
     * @return 公开 VO
     */
    public ArticlePublicVO toPublicVO(ArticleDetailVO detail) {
        if (detail == null) {
            return null;
        }
        ArticlePublicVO vo = new ArticlePublicVO();
        vo.setId(detail.getId());
        vo.setTitle(detail.getTitle());
        vo.setSlug(detail.getSlug());
        vo.setSummary(detail.getSummary());
        vo.setCoverUrl(detail.getCoverUrl());
        vo.setStatus(detail.getStatus());
        vo.setSourceType(detail.getSourceType());
        vo.setAllowComment(detail.getAllowComment());
        vo.setIsPinned(detail.getIsPinned());
        vo.setAuthor(detail.getAuthor());
        vo.setCategory(detail.getCategory());
        vo.setColumn(detail.getColumn());
        vo.setTags(detail.getTags());
        vo.setWordCount(detail.getWordCount());
        vo.setReadCount(detail.getReadCount());
        vo.setLikeCount(detail.getLikeCount());
        vo.setCommentCount(detail.getCommentCount());
        vo.setPublishTime(detail.getPublishTime());
        vo.setCreateTime(detail.getCreateTime());
        vo.setUpdateTime(detail.getUpdateTime());
        vo.setContentHtml(detail.getContentHtml());
        return vo;
    }

    /**
     * 批量加载文章正文。
     *
     * @param articleIds 文章 ID 列表
     * @return 正文 Map
     */
    private Map<Long, BlogArticleBody> loadBodies(List<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return Map.of();
        }
        return bodyMapper.selectList(new LambdaQueryWrapper<BlogArticleBody>()
                        .in(BlogArticleBody::getArticleId, articleIds))
                .stream()
                .collect(Collectors.toMap(BlogArticleBody::getArticleId, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量加载用户。
     *
     * @param authorIds 用户 ID 集合
     * @return 用户 Map
     */
    private Map<Long, SysUser> loadUsers(Set<Long> authorIds) {
        if (authorIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, authorIds))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量加载分类。
     *
     * @param categoryIds 分类 ID 集合
     * @return 分类 Map
     */
    private Map<Long, BlogCategory> loadCategories(Set<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>().in(BlogCategory::getId, categoryIds))
                .stream()
                .collect(Collectors.toMap(BlogCategory::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量加载专栏。
     *
     * @param columnIds 专栏 ID 集合
     * @return 专栏 Map
     */
    private Map<Long, BlogColumn> loadColumns(Set<Long> columnIds) {
        if (columnIds.isEmpty()) {
            return Map.of();
        }
        return columnMapper.selectList(new LambdaQueryWrapper<BlogColumn>().in(BlogColumn::getId, columnIds))
                .stream()
                .collect(Collectors.toMap(BlogColumn::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量加载文章标签。
     *
     * @param articleIds 文章 ID 列表
     * @return 文章 ID -> 标签列表
     */
    private Map<Long, List<TagVO>> loadTagsByArticleIds(List<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return Map.of();
        }
        List<BlogArticleTag> rels = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                .in(BlogArticleTag::getArticleId, articleIds));
        if (rels.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Long>> articleTagIds = new HashMap<>();
        for (BlogArticleTag rel : rels) {
            articleTagIds.computeIfAbsent(rel.getArticleId(), k -> new ArrayList<>()).add(rel.getTagId());
        }
        Set<Long> allTagIds = articleTagIds.values().stream().flatMap(Collection::stream)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BlogTag> tagMap = allTagIds.isEmpty()
                ? Map.of()
                : tagMapper.selectList(new LambdaQueryWrapper<BlogTag>().in(BlogTag::getId, allTagIds))
                .stream()
                .collect(Collectors.toMap(BlogTag::getId, Function.identity(), (a, b) -> a));

        Map<Long, List<TagVO>> result = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : articleTagIds.entrySet()) {
            List<TagVO> tags = entry.getValue().stream()
                    .map(tagMap::get)
                    .filter(Objects::nonNull)
                    .map(this::toTagVO)
                    .toList();
            result.put(entry.getKey(), tags);
        }
        return result;
    }

    /**
     * 转换用户 VO。
     *
     * @param u 用户实体
     * @return 用户 VO
     */
    private UserVO toUserVO(SysUser u) {
        if (u == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setProfile(u.getProfile());
        return vo;
    }

    /**
     * 转换分类 VO。
     *
     * @param c 分类实体
     * @return 分类 VO
     */
    private CategoryVO toCategoryVO(BlogCategory c) {
        if (c == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setParentId(c.getParentId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setDescription(c.getDescription());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        return vo;
    }

    /**
     * 转换专栏 VO。
     *
     * @param c 专栏实体
     * @return 专栏 VO
     */
    private ColumnVO toColumnVO(BlogColumn c) {
        if (c == null) {
            return null;
        }
        ColumnVO vo = new ColumnVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setCoverUrl(c.getCoverUrl());
        vo.setDescription(c.getDescription());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        return vo;
    }

    /**
     * 转换标签 VO。
     *
     * @param t 标签实体
     * @return 标签 VO
     */
    private TagVO toTagVO(BlogTag t) {
        TagVO vo = new TagVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setSlug(t.getSlug());
        vo.setColor(t.getColor());
        vo.setDescription(t.getDescription());
        return vo;
    }
}
