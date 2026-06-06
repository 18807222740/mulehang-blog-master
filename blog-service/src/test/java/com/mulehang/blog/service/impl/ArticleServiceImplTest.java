package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mulehang.blog.cache.MultiLevelCache;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.converter.ArticleConverter;
import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.entity.BlogArticleBody;
import com.mulehang.blog.entity.BlogArticleTag;
import com.mulehang.blog.entity.BlogCategory;
import com.mulehang.blog.entity.BlogTag;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.mapper.BlogArticleBodyMapper;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.mapper.BlogArticleTagMapper;
import com.mulehang.blog.mapper.BlogCategoryMapper;
import com.mulehang.blog.mapper.BlogColumnMapper;
import com.mulehang.blog.mapper.BlogTagMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.metrics.BlogMetrics;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.ArticleReadCountService;
import com.mulehang.blog.service.CacheConsistencyService;
import com.mulehang.blog.service.HotArticleService;
import com.mulehang.blog.service.article.ArticleAssembler;
import com.mulehang.blog.service.article.ArticleMqFacade;
import com.mulehang.blog.service.article.ArticlePermissionHelper;
import com.mulehang.blog.util.MarkdownRenderer;
import com.mulehang.blog.vo.ArticleDetailVO;
import com.mulehang.blog.vo.ArticleListVO;
import com.mulehang.blog.vo.UserInfoVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ArticleServiceImpl 单元测试。
 */
class ArticleServiceImplTest {

        private static final Long CURRENT_USER_ID = 99L;

        private BlogArticleMapper articleMapper;

        private BlogArticleBodyMapper bodyMapper;

        private BlogArticleTagMapper articleTagMapper;

        private BlogCategoryMapper categoryMapper;

        private BlogColumnMapper columnMapper;

        private BlogTagMapper tagMapper;

        private SysUserMapper userMapper;

        private ArticleConverter articleConverter;

        private MarkdownRenderer markdownRenderer;

        private HotArticleService hotArticleService;

        private CacheConsistencyService cacheConsistencyService;

        private MultiLevelCache multiLevelCache;

        private BlogMetrics blogMetrics;

        private ArticleReadCountService articleReadCountService;

        private ArticleAssembler articleAssembler;

        private ArticlePermissionHelper permissionHelper;

        private ArticleMqFacade articleMqFacade;

        private ArticleServiceImpl articleService;

        /**
         * 初始化测试依赖。
         */
        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUp() {
                articleMapper = Mockito.mock(BlogArticleMapper.class);
                bodyMapper = Mockito.mock(BlogArticleBodyMapper.class);
                articleTagMapper = Mockito.mock(BlogArticleTagMapper.class);
                categoryMapper = Mockito.mock(BlogCategoryMapper.class);
                columnMapper = Mockito.mock(BlogColumnMapper.class);
                tagMapper = Mockito.mock(BlogTagMapper.class);
                userMapper = Mockito.mock(SysUserMapper.class);
                articleConverter = Mockito.mock(ArticleConverter.class);
                markdownRenderer = Mockito.mock(MarkdownRenderer.class);
                hotArticleService = Mockito.mock(HotArticleService.class);
                cacheConsistencyService = Mockito.mock(CacheConsistencyService.class);
                multiLevelCache = Mockito.mock(MultiLevelCache.class);
                blogMetrics = Mockito.mock(BlogMetrics.class);
                articleReadCountService = Mockito.mock(ArticleReadCountService.class);
                articleAssembler = Mockito.mock(ArticleAssembler.class);
                permissionHelper = Mockito.mock(ArticlePermissionHelper.class);
                articleMqFacade = Mockito.mock(ArticleMqFacade.class);
                when(permissionHelper.requireCurrentUserId()).thenReturn(CURRENT_USER_ID);
                when(permissionHelper.isAdmin()).thenReturn(true);
                articleService = new ArticleServiceImpl(articleMapper, bodyMapper, articleTagMapper,
                                articleConverter, markdownRenderer, cacheConsistencyService, multiLevelCache,
                                blogMetrics, articleReadCountService, hotArticleService, articleAssembler,
                                permissionHelper, articleMqFacade);

                UserContext.setCurrentUser(UserInfoVO.builder()
                                .id(CURRENT_USER_ID)
                                .username("tester")
                                .roles(List.of("ADMIN"))
                                .build());
        }

        /**
         * 清理用户上下文。
         */
        @AfterEach
        void tearDown() {
                UserContext.clear();
        }

        /**
         * 验证创建文章时会写入基础表/正文/标签并执行缓存淘汰。
         */
        @Test
        void createArticle_shouldPersistAndEvictCache() {
                ArticleCreateDTO dto = new ArticleCreateDTO();
                dto.setTitle("Test Title");
                dto.setContentMd("a b");
                dto.setTagIds(List.of(1L, 2L));

                BlogArticle article = new BlogArticle();
                article.setId(100L);
                article.setSlug("test-title");
                when(articleConverter.toArticleEntity(dto)).thenReturn(article);

                BlogArticleBody body = new BlogArticleBody();
                when(articleConverter.toArticleBodyEntity(dto)).thenReturn(body);
                when(markdownRenderer.renderToHtml("a b")).thenReturn("<p>a b</p>");

                BlogArticleTag rel1 = new BlogArticleTag();
                rel1.setTagId(1L);
                BlogArticleTag rel2 = new BlogArticleTag();
                rel2.setTagId(2L);
                when(articleConverter.tagIdsToArticleTags(dto.getTagIds())).thenReturn(List.of(rel1, rel2));

                Long id = articleService.createArticle(dto);

                assertEquals(100L, id);

                ArgumentCaptor<BlogArticle> articleCaptor = ArgumentCaptor.forClass(BlogArticle.class);
                verify(articleMapper).insert(articleCaptor.capture());
                BlogArticle inserted = articleCaptor.getValue();
                assertEquals(CURRENT_USER_ID, inserted.getAuthorId());
                assertEquals(0, inserted.getStatus());
                assertEquals(1, inserted.getSourceType());
                assertEquals(1, inserted.getAllowComment());
                assertEquals(0, inserted.getIsPinned());
                assertEquals(2, inserted.getWordCount());
                assertEquals(0L, inserted.getReadCount());
                assertEquals(0, inserted.getLikeCount());
                assertEquals(0, inserted.getCommentCount());
                assertNull(inserted.getPublishTime());

                ArgumentCaptor<BlogArticleBody> bodyCaptor = ArgumentCaptor.forClass(BlogArticleBody.class);
                verify(bodyMapper).insert(bodyCaptor.capture());
                BlogArticleBody insertedBody = bodyCaptor.getValue();
                assertEquals(100L, insertedBody.getArticleId());
                assertEquals("a b", insertedBody.getContentMd());
                assertEquals("<p>a b</p>", insertedBody.getContentHtml());

                ArgumentCaptor<BlogArticleTag> tagCaptor = ArgumentCaptor.forClass(BlogArticleTag.class);
                verify(articleTagMapper, atLeastOnce()).insert(tagCaptor.capture());
                for (BlogArticleTag rel : tagCaptor.getAllValues()) {
                        assertEquals(100L, rel.getArticleId());
                        assertNotNull(rel.getTagId());
                }

                verify(cacheConsistencyService).evictArticleDetail(100L);
        }

        /**
         * 验证更新文章时正文、标签与缓存一致性逻辑被触发。
         */
        @Test
        void updateArticle_shouldUpdateBodyAndEvictCache() {
                BlogArticle existing = new BlogArticle();
                existing.setId(10L);
                existing.setStatus(0);
                when(articleMapper.selectById(10L)).thenReturn(existing);

                ArticleUpdateDTO dto = new ArticleUpdateDTO();
                dto.setTitle("new title");
                dto.setContentMd("new content");
                dto.setStatus(1);
                dto.setTagIds(List.of(9L));

                when(articleTagMapper.selectAllTagIdsByArticleId(10L)).thenReturn(List.of(9L));

                BlogArticleBody existingBody = new BlogArticleBody();
                existingBody.setId(3L);
                when(bodyMapper.selectOne(ArgumentMatchers.<LambdaQueryWrapper<BlogArticleBody>>any()))
                                .thenReturn(existingBody);
                when(markdownRenderer.renderToHtml("new content")).thenReturn("<p>new</p>");

                BlogArticleTag rel = new BlogArticleTag();
                rel.setTagId(9L);
                when(articleConverter.tagIdsToArticleTags(dto.getTagIds())).thenReturn(List.of(rel));

                articleService.updateArticle(10L, dto);

                ArgumentCaptor<BlogArticle> patchCaptor = ArgumentCaptor.forClass(BlogArticle.class);
                verify(articleMapper, atLeastOnce()).updateById(patchCaptor.capture());
                assertTrue(patchCaptor.getAllValues().stream().anyMatch(p -> p.getPublishTime() != null));

                ArgumentCaptor<BlogArticleBody> bodyCaptor = ArgumentCaptor.forClass(BlogArticleBody.class);
                verify(bodyMapper).updateById(bodyCaptor.capture());
                BlogArticleBody patchedBody = bodyCaptor.getValue();
                assertEquals(3L, patchedBody.getId());
                assertEquals("new content", patchedBody.getContentMd());
                assertEquals("<p>new</p>", patchedBody.getContentHtml());

                verify(articleTagMapper)
                                .delete(ArgumentMatchers.any());
                verify(articleTagMapper).update(ArgumentMatchers.isNull(), ArgumentMatchers.any());
                verify(articleTagMapper, never()).insert(rel);
                verify(cacheConsistencyService).evictArticleDetail(10L);
        }

        /**
         * 验证发布文章会更新状态并触发缓存淘汰。
         */
        @Test
        void publishArticle_shouldUpdateStatusAndEvictCache() {
                BlogArticle existing = new BlogArticle();
                existing.setId(20L);
                existing.setStatus(0);
                when(articleMapper.selectById(20L)).thenReturn(existing);

                articleService.publishArticle(20L);

                ArgumentCaptor<BlogArticle> patchCaptor = ArgumentCaptor.forClass(BlogArticle.class);
                verify(articleMapper).updateById(patchCaptor.capture());
                BlogArticle patch = patchCaptor.getValue();
                assertEquals(20L, patch.getId());
                assertEquals(1, patch.getStatus());
                assertNotNull(patch.getPublishTime());

                verify(cacheConsistencyService).evictArticleDetail(20L);
        }

        /**
         * 验证详情查询会走多级缓存并累计热榜阅读量。
         */
        @Test
        void getArticleDetail_shouldUseCacheAndIncrementHot() {
                ArticleDetailVO detail = new ArticleDetailVO();
                detail.setId(30L);
                detail.setReadCount(0L);

                String cacheKey = RedisKeys.ARTICLE_DETAIL_PREFIX + 30L;
                when(multiLevelCache.get(eq(cacheKey), eq(ArticleDetailVO.class),
                                ArgumentMatchers.any())).thenReturn(detail);
                when(articleReadCountService.resolveDisplayCount(30L, 0L)).thenReturn(1L);

                ArticleDetailVO result = articleService.getArticleDetail(30L);

                assertSame(detail, result);
                verify(articleReadCountService).recordRead(30L);
        }

        /**
         * 验证列表查询在标签过滤无命中时直接返回空结果。
         */
        @Test
        void listArticles_shouldReturnEmptyWhenTagFilterNoMatch() {
                ArticleQueryDTO query = new ArticleQueryDTO();
                query.setTagId(99L);

                when(articleTagMapper.selectList(ArgumentMatchers.<LambdaQueryWrapper<BlogArticleTag>>any()))
                                .thenReturn(Collections.emptyList());

                PageResult<ArticleListVO> result = articleService.listArticles(query);

                assertEquals(0, result.getTotal());
                assertEquals(1L, result.getPageNo());
                assertEquals(10L, result.getPageSize());
                assertTrue(result.getList().isEmpty());
                verify(articleMapper, never())
                                .selectPage(ArgumentMatchers.<Page<BlogArticle>>any(),
                                                ArgumentMatchers.<LambdaQueryWrapper<BlogArticle>>any());
        }

        /**
         * 验证分页列表查询会组装作者、分类与标签信息。
         */
        @Test
        void listArticles_shouldReturnPageWithMappedData() {
                ArticleQueryDTO query = new ArticleQueryDTO();
                query.setPageNo(1);
                query.setPageSize(10);

                BlogArticle article = new BlogArticle();
                article.setId(100L);
                article.setTitle("Title A");
                article.setAuthorId(1L);
                article.setCategoryId(10L);
                article.setReadCount(5L);
                article.setLikeCount(1);
                article.setCommentCount(2);

                Page<BlogArticle> page = new Page<>(1L, 10L);
                page.setRecords(List.of(article));
                page.setTotal(1);
                when(articleMapper.selectPage(ArgumentMatchers.<Page<BlogArticle>>any(),
                                ArgumentMatchers.<LambdaQueryWrapper<BlogArticle>>any()))
                                .thenReturn(page);

                ArticleListVO listVO = new ArticleListVO();
                listVO.setId(100L);
                listVO.setTitle("Title A");
                when(articleAssembler.buildListVO(List.of(article))).thenReturn(List.of(listVO));

                PageResult<ArticleListVO> result = articleService.listArticles(query);

                assertEquals(1L, result.getTotal());
                assertEquals(1, result.getList().size());
                ArticleListVO vo = result.getList().getFirst();
                assertEquals(100L, vo.getId());
                assertEquals("Title A", vo.getTitle());
                verify(articleAssembler).buildListVO(List.of(article));
        }

        /**
         * 验证删除文章时会执行逻辑删除与缓存淘汰。
         */
        @Test
        void deleteArticle_shouldDeleteAndEvictCache() {
                BlogArticle existing = new BlogArticle();
                existing.setId(40L);
                existing.setAuthorId(CURRENT_USER_ID);
                when(articleMapper.selectById(40L)).thenReturn(existing);

                articleService.deleteArticle(40L);

                verify(articleMapper).deleteById(40L);
                verify(cacheConsistencyService).evictArticleDetail(40L);
        }

}
