package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mulehang.blog.cache.MultiLevelCache;
import com.mulehang.blog.converter.ArticleConverter;
import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.entity.*;
import com.mulehang.blog.exception.BusinessException;
import com.mulehang.blog.mapper.*;
import com.mulehang.blog.metrics.BlogMetrics;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.ArticleReadCountService;
import com.mulehang.blog.service.ArticleService;
import com.mulehang.blog.service.CacheConsistencyService;
import com.mulehang.blog.service.HotArticleService;
import com.mulehang.blog.service.article.ArticleAssembler;
import com.mulehang.blog.service.article.ArticleMqFacade;
import com.mulehang.blog.service.article.ArticlePermissionHelper;
import com.mulehang.blog.util.MarkdownRenderer;
import com.mulehang.blog.vo.ArticleDetailVO;
import com.mulehang.blog.vo.ArticleListVO;
import com.mulehang.blog.vo.ArticlePublicVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章 Service（写操作 + 读操作协调层）。
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int SOURCE_ORIGINAL = 1;

    private final BlogArticleMapper articleMapper;
    private final BlogArticleBodyMapper bodyMapper;
    private final BlogArticleTagMapper articleTagMapper;
    private final ArticleConverter articleConverter;
    private final MarkdownRenderer markdownRenderer;
    private final CacheConsistencyService cacheConsistencyService;
    private final MultiLevelCache multiLevelCache;
    private final BlogMetrics blogMetrics;
    private final ArticleReadCountService articleReadCountService;
    private final HotArticleService hotArticleService;
    private final ArticleAssembler articleAssembler;
    private final ArticlePermissionHelper permissionHelper;
    private final ArticleMqFacade articleMqFacade;

    /**
     * 构造注入。
     */
    public ArticleServiceImpl(BlogArticleMapper articleMapper,
                              BlogArticleBodyMapper bodyMapper,
                              BlogArticleTagMapper articleTagMapper,
                              ArticleConverter articleConverter,
                              MarkdownRenderer markdownRenderer,
                              CacheConsistencyService cacheConsistencyService,
                              MultiLevelCache multiLevelCache,
                              BlogMetrics blogMetrics,
                              ArticleReadCountService articleReadCountService,
                              HotArticleService hotArticleService,
                              ArticleAssembler articleAssembler,
                              ArticlePermissionHelper permissionHelper,
                              ArticleMqFacade articleMqFacade) {
        this.articleMapper = articleMapper;
        this.bodyMapper = bodyMapper;
        this.articleTagMapper = articleTagMapper;
        this.articleConverter = articleConverter;
        this.markdownRenderer = markdownRenderer;
        this.cacheConsistencyService = cacheConsistencyService;
        this.multiLevelCache = multiLevelCache;
        this.blogMetrics = blogMetrics;
        this.articleReadCountService = articleReadCountService;
        this.hotArticleService = hotArticleService;
        this.articleAssembler = articleAssembler;
        this.permissionHelper = permissionHelper;
        this.articleMqFacade = articleMqFacade;
    }

    /**
     * 创建文章。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(ArticleCreateDTO dto) {
        if (dto == null) {
            throw BusinessException.badRequest("dto 为空");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw BusinessException.badRequest("标题为空");
        }
        if (dto.getContentMd() == null) {
            throw BusinessException.badRequest("内容为空");
        }

        Long currentUserId = permissionHelper.requireCurrentUserId();
        BlogArticle article = articleConverter.toArticleEntity(dto);
        article.setAuthorId(currentUserId);
        article.setStatus(dto.getStatus() == null ? STATUS_DRAFT : dto.getStatus());
        article.setSourceType(dto.getSourceType() == null ? SOURCE_ORIGINAL : dto.getSourceType());
        article.setAllowComment(dto.getAllowComment() == null ? 1 : dto.getAllowComment());
        article.setIsPinned(dto.getIsPinned() == null ? 0 : dto.getIsPinned());

        if (article.getSlug() == null || article.getSlug().isBlank()) {
            article.setSlug(generateSlug(dto.getTitle()));
        }

        article.setWordCount(countWords(dto.getContentMd()));
        article.setReadCount(0L);
        article.setLikeCount(0);
        article.setCommentCount(0);
        if (Objects.equals(article.getStatus(), STATUS_PUBLISHED)) {
            article.setPublishTime(LocalDateTime.now());
        }

        articleMapper.insert(article);

        BlogArticleBody body = articleConverter.toArticleBodyEntity(dto);
        body.setArticleId(article.getId());
        body.setContentMd(dto.getContentMd());
        body.setContentHtml(markdownRenderer.renderToHtml(dto.getContentMd()));
        bodyMapper.insert(body);

        saveArticleTags(article.getId(), dto.getTagIds());

        if (Objects.equals(article.getStatus(), STATUS_PUBLISHED)) {
            blogMetrics.incrementArticlePublish();
        }

        cacheConsistencyService.evictArticleDetail(article.getId());
        articleMqFacade.sendUpsertIfEnabled(article.getId(), "create");
        return article.getId();
    }

    /**
     * 更新文章。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long id, ArticleUpdateDTO dto) {
        if (id == null) {
            throw BusinessException.badRequest("id 为空");
        }
        if (dto == null) {
            throw BusinessException.badRequest("dto 为空");
        }

        BlogArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.notFound("找不到文章: " + id);
        }
        permissionHelper.assertCanOperate(existing);

        BlogArticle patch = new BlogArticle();
        patch.setId(id);
        if (dto.getTitle() != null) patch.setTitle(dto.getTitle());
        if (dto.getSlug() != null) patch.setSlug(dto.getSlug());
        if (dto.getSummary() != null) patch.setSummary(dto.getSummary());
        if (dto.getCoverUrl() != null) patch.setCoverUrl(dto.getCoverUrl());
        if (dto.getStatus() != null) patch.setStatus(dto.getStatus());
        if (dto.getSourceType() != null) patch.setSourceType(dto.getSourceType());
        if (dto.getAllowComment() != null) patch.setAllowComment(dto.getAllowComment());
        if (dto.getIsPinned() != null) patch.setIsPinned(dto.getIsPinned());
        if (dto.getCategoryId() != null) patch.setCategoryId(dto.getCategoryId());
        if (dto.getColumnId() != null) patch.setColumnId(dto.getColumnId());
        if (dto.getContentMd() != null) patch.setWordCount(countWords(dto.getContentMd()));

        articleMapper.updateById(patch);

        if (dto.getContentMd() != null) {
            BlogArticleBody body = bodyMapper.selectOne(new LambdaQueryWrapper<BlogArticleBody>()
                    .eq(BlogArticleBody::getArticleId, id));
            if (body == null) {
                body = new BlogArticleBody();
                body.setArticleId(id);
                body.setContentMd(dto.getContentMd());
                body.setContentHtml(markdownRenderer.renderToHtml(dto.getContentMd()));
                bodyMapper.insert(body);
            } else {
                BlogArticleBody bodyPatch = new BlogArticleBody();
                bodyPatch.setId(body.getId());
                bodyPatch.setContentMd(dto.getContentMd());
                bodyPatch.setContentHtml(markdownRenderer.renderToHtml(dto.getContentMd()));
                bodyMapper.updateById(bodyPatch);
            }
        }

        if (dto.getTagIds() != null) {
            updateArticleTags(id, dto.getTagIds());
        }

        if (dto.getStatus() != null
                && Objects.equals(dto.getStatus(), STATUS_PUBLISHED)
                && existing.getPublishTime() == null) {
            BlogArticle publishPatch = new BlogArticle();
            publishPatch.setId(id);
            publishPatch.setPublishTime(LocalDateTime.now());
            articleMapper.updateById(publishPatch);
        }

        cacheConsistencyService.evictArticleDetail(id);
        articleMqFacade.sendUpsertIfEnabled(id, "update");
    }

    /**
     * 发布文章。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long id) {
        if (id == null) {
            throw BusinessException.badRequest("id 为空");
        }
        BlogArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.notFound("文章未找到: " + id);
        }
        permissionHelper.assertCanOperate(existing);
        if (Objects.equals(existing.getStatus(), STATUS_PUBLISHED) && existing.getPublishTime() != null) {
            return;
        }
        BlogArticle patch = new BlogArticle();
        patch.setId(id);
        patch.setStatus(STATUS_PUBLISHED);
        patch.setPublishTime(LocalDateTime.now());
        articleMapper.updateById(patch);

        cacheConsistencyService.evictArticleDetail(id);
        blogMetrics.incrementArticlePublish();
        articleMqFacade.sendUpsertIfEnabled(id, "publish");
    }

    /**
     * 获取文章详情。
     */
    @Override
    public ArticleDetailVO getArticleDetail(Long id) {
        if (id == null) {
            throw BusinessException.badRequest("id 为空");
        }
        String cacheKey = RedisKeys.ARTICLE_DETAIL_PREFIX + id;
        ArticleDetailVO vo = multiLevelCache.get(cacheKey, ArticleDetailVO.class, () -> {
            BlogArticle article = articleMapper.selectById(id);
            if (article == null) {
                return null;
            }
            return articleAssembler.buildDetail(article);
        });
        if (vo == null) {
            throw BusinessException.notFound("通过 ID 找不到文章: " + id);
        }
        return applyReadCount(vo);
    }

    /**
     * 根据 slug 获取文章详情。
     */
    @Override
    public ArticleDetailVO getArticleBySlug(String slug) {
        BlogArticle article = findPublishedBySlug(slug);
        String cacheKey = RedisKeys.ARTICLE_DETAIL_PREFIX + article.getId();
        ArticleDetailVO vo = multiLevelCache.get(cacheKey, ArticleDetailVO.class, () -> articleAssembler.buildDetail(article));
        return applyReadCount(vo);
    }

    /**
     * 根据 slug 获取前台文章详情。
     */
    @Override
    public ArticlePublicVO getPublicArticleBySlug(String slug) {
        BlogArticle article = findPublishedBySlug(slug);
        String cacheKey = RedisKeys.ARTICLE_DETAIL_PREFIX + article.getId();
        ArticleDetailVO detailVO = multiLevelCache.get(cacheKey, ArticleDetailVO.class, () -> articleAssembler.buildDetail(article));
        ArticlePublicVO vo = articleAssembler.toPublicVO(applyReadCount(detailVO));
        return vo;
    }

    /**
     * 获取热榜文章。
     */
    @Override
    public List<ArticleListVO> listHotArticles(int topN) {
        List<Long> ids = hotArticleService.getHotArticleIds(topN);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<BlogArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                .in(BlogArticle::getId, ids));
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, BlogArticle> map = articles.stream()
                .collect(Collectors.toMap(BlogArticle::getId, a -> a, (a, b) -> a));
        List<BlogArticle> ordered = ids.stream().map(map::get).filter(Objects::nonNull).toList();
        return articleAssembler.buildListVO(ordered);
    }

    /**
     * 分页查询文章。
     */
    @Override
    public PageResult<ArticleListVO> listArticles(ArticleQueryDTO query) {
        if (query == null) {
            query = new ArticleQueryDTO();
        }
        long pageNo = query.getPageNo() == null ? 1L : query.getPageNo();
        long pageSize = query.getPageSize() == null ? 10L : query.getPageSize();

        Page<BlogArticle> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<BlogArticle> qw = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) qw.eq(BlogArticle::getStatus, query.getStatus());
        if (query.getCategoryId() != null) qw.eq(BlogArticle::getCategoryId, query.getCategoryId());
        if (query.getColumnId() != null) qw.eq(BlogArticle::getColumnId, query.getColumnId());
        if (query.getAuthorId() != null) qw.eq(BlogArticle::getAuthorId, query.getAuthorId());
        String keyword = query.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            final String kw = keyword;
            qw.and(w -> w.like(BlogArticle::getTitle, kw).or().like(BlogArticle::getSummary, kw));
        }

        if (query.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                            .eq(BlogArticleTag::getTagId, query.getTagId()))
                    .stream().map(BlogArticleTag::getArticleId).distinct().toList();
            if (articleIds.isEmpty()) {
                return PageResult.empty(pageNo, pageSize);
            }
            qw.in(BlogArticle::getId, articleIds);
        }

        applySort(qw, query.getSortBy(), query.getSortOrder());
        Page<BlogArticle> resultPage = articleMapper.selectPage(page, qw);
        List<BlogArticle> records = resultPage.getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.empty(pageNo, pageSize);
        }

        PageResult<ArticleListVO> pr = new PageResult<>();
        pr.setList(articleAssembler.buildListVO(records));
        pr.setTotal(resultPage.getTotal());
        pr.setPageNo(pageNo);
        pr.setPageSize(pageSize);
        return pr;
    }

    /**
     * 删除文章。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        if (id == null) {
            throw BusinessException.badRequest("id 为空");
        }
        BlogArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.notFound("文章未找到: " + id);
        }
        permissionHelper.assertCanOperate(existing);
        articleMapper.deleteById(id);
        cacheConsistencyService.evictArticleDetail(id);
        articleMqFacade.sendDeleteIfEnabled(id);
    }

    /**
     * 记录阅读并返回带最新阅读量的 VO。
     *
     * @param vo 文章详情 VO
     * @return 更新阅读量后的 VO
     */
    private ArticleDetailVO applyReadCount(ArticleDetailVO vo) {
        articleReadCountService.recordRead(vo.getId());
        vo.setReadCount(articleReadCountService.resolveDisplayCount(vo.getId(), vo.getReadCount()));
        return vo;
    }

    /**
     * 查找已发布文章。
     *
     * @param slug slug
     * @return 文章实体
     */
    private BlogArticle findPublishedBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw BusinessException.badRequest("slug 为空");
        }
        BlogArticle article = articleMapper.selectOne(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getSlug, slug)
                .eq(BlogArticle::getStatus, STATUS_PUBLISHED));
        if (article == null) {
            throw BusinessException.notFound("通过 slug 找不到文章: " + slug);
        }
        return article;
    }

    /**
     * 更新文章标签关联。
     *
     * @param id     文章 ID
     * @param tagIds 标签 ID 列表
     */
    private void updateArticleTags(Long id, List<Long> tagIds) {
        List<Long> inputTagIds = tagIds.stream().filter(Objects::nonNull).distinct().toList();
        List<Long> existingTagIds = articleTagMapper.selectAllTagIdsByArticleId(id);
        Set<Long> existingTagIdSet = existingTagIds == null ? Collections.emptySet() : new HashSet<>(existingTagIds);

        articleTagMapper.delete(new QueryWrapper<BlogArticleTag>().eq("article_id", id));

        if (!inputTagIds.isEmpty()) {
            List<Long> restoreTagIds = inputTagIds.stream().filter(existingTagIdSet::contains).toList();
            if (!restoreTagIds.isEmpty()) {
                UpdateWrapper<BlogArticleTag> restoreWrapper = new UpdateWrapper<>();
                restoreWrapper.eq("article_id", id)
                        .in("tag_id", restoreTagIds)
                        .set("is_deleted", 0)
                        .set("update_time", LocalDateTime.now());
                articleTagMapper.update(null, restoreWrapper);
            }
            List<Long> newTagIds = inputTagIds.stream().filter(tagId -> !existingTagIdSet.contains(tagId)).toList();
            saveArticleTags(id, newTagIds);
        }
    }

    /**
     * 保存文章标签。
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        List<BlogArticleTag> rels = articleConverter.tagIdsToArticleTags(tagIds);
        if (rels == null || rels.isEmpty()) {
            return;
        }
        for (BlogArticleTag rel : rels) {
            rel.setArticleId(articleId);
            articleTagMapper.insert(rel);
        }
    }

    /**
     * 应用排序。
     */
    private void applySort(LambdaQueryWrapper<BlogArticle> qw, String sortBy, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (sortBy == null || sortBy.isBlank()) {
            qw.orderByDesc(BlogArticle::getPublishTime).orderByDesc(BlogArticle::getCreateTime);
            return;
        }
        switch (sortBy) {
            case "publishTime" -> qw.orderBy(true, asc, BlogArticle::getPublishTime);
            case "createTime" -> qw.orderBy(true, asc, BlogArticle::getCreateTime);
            case "readCount" -> qw.orderBy(true, asc, BlogArticle::getReadCount);
            default -> qw.orderByDesc(BlogArticle::getPublishTime).orderByDesc(BlogArticle::getCreateTime);
        }
    }

    /**
     * 统计 Markdown 字数。
     */
    private int countWords(String contentMd) {
        if (contentMd == null || contentMd.isBlank()) {
            return 0;
        }
        int cnt = 0;
        for (int i = 0; i < contentMd.length(); i++) {
            if (!Character.isWhitespace(contentMd.charAt(i))) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * 生成 slug。
     */
    private String generateSlug(String title) {
        String base = title == null ? "" : title.trim().toLowerCase(Locale.ROOT);
        base = base.replaceAll("[^a-z0-9\\s-]", "");
        base = base.replaceAll("\\s+", "-");
        base = base.replaceAll("-+", "-");
        if (base.isBlank()) {
            base = "article";
        }
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return base + "-" + suffix;
    }
}
