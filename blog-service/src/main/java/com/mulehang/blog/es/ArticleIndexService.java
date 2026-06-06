package com.mulehang.blog.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.entity.BlogArticleBody;
import com.mulehang.blog.entity.BlogArticleTag;
import com.mulehang.blog.entity.BlogCategory;
import com.mulehang.blog.entity.BlogTag;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.es.document.ArticleDocument;
import com.mulehang.blog.es.init.BlogArticleIndexInitializer;
import com.mulehang.blog.mapper.BlogArticleBodyMapper;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.mapper.BlogArticleTagMapper;
import com.mulehang.blog.mapper.BlogCategoryMapper;
import com.mulehang.blog.mapper.BlogTagMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 文章索引同步服务（MySQL -> Elasticsearch）。
 *
 * <p>为什么要单独做一个同步服务？</p>
 * <ul>
 *     <li>业务写入发生在 MySQL（事务），ES 属于“衍生数据”（可重建）。</li>
 *     <li>如果在事务内直接写 ES，一旦事务回滚，会造成 ES 与 MySQL 不一致。</li>
 *     <li>因此这里采用“事务提交后（AFTER_COMMIT）再同步 ES”的策略。</li>
 * </ul>
 *
 * <p>同步规则（学习项目的简单策略）：</p>
 * <ul>
 *     <li>文章状态为“已发布”时：写入/更新 ES 文档。</li>
 *     <li>文章未发布（草稿）或文章被删除时：从 ES 删除文档，避免搜索到草稿或脏数据。</li>
 * </ul>
 *
 * <p>容错策略：</p>
 * <ul>
 *     <li>ES 属于可选组件，因此任何 ES 异常都不应影响主业务。</li>
 *     <li>同步失败只记录 warn 日志，后续可通过“补偿任务”或“手动重建索引”兜底。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(ElasticsearchClient.class)
@SuppressWarnings("unused") // Spring Bean 由容器管理，静态分析可能误报“未使用”
public class ArticleIndexService {

    /**
     * 文章状态：已发布（与 {@code ArticleServiceImpl} 里保持一致）。
     */
    private static final int STATUS_PUBLISHED = 1;

    /**
     * ES 同步专用线程池（单线程串行）。
     *
     * <p>说明：</p>
     * <ul>
     *     <li>索引同步不是强一致需求，串行执行能减少并发引起的覆盖与压力。</li>
     *     <li>线程设为 daemon，避免阻止 JVM 退出。</li>
     * </ul>
     */
    private final ExecutorService indexExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("es-article-indexer");
        t.setDaemon(true);
        return t;
    });

    private final ElasticsearchClient esClient;
    private final BlogArticleMapper articleMapper;
    private final BlogArticleBodyMapper bodyMapper;
    private final BlogArticleTagMapper articleTagMapper;
    private final BlogTagMapper tagMapper;
    private final BlogCategoryMapper categoryMapper;
    private final SysUserMapper userMapper;
    private final BlogArticleIndexInitializer indexInitializer;

    /**
     * 在事务提交后同步文章索引。
     *
     * <p>调用方通常在“创建/更新/发布”文章后调用该方法。</p>
     *
     * @param articleId 文章 ID
     */
    public void syncArticleAfterCommit(Long articleId) {
        if (articleId == null) {
            return;
        }
        Runnable task = () -> {
            try {
                syncArticle(articleId);
            } catch (Exception e) {
                // 容错：不影响主流程
                log.warn("ES 同步文章失败: articleId={}, msg={}", articleId, e.getMessage());
                log.debug("ES 同步文章异常详情", e);
            }
        };

        // 确保 ES 同步发生在事务提交后，避免回滚导致不一致
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    indexExecutor.execute(task);
                }
            });
        } else {
            indexExecutor.execute(task);
        }
    }

    /**
     * 在事务提交后删除文章索引。
     *
     * <p>调用方通常在“删除文章”后调用该方法。</p>
     *
     * @param articleId 文章 ID
     */
    public void deleteArticleAfterCommit(Long articleId) {
        if (articleId == null) {
            return;
        }
        Runnable task = () -> {
            try {
                deleteArticleDoc(articleId);
            } catch (Exception e) {
                // 容错：不影响主流程
                log.warn("ES 删除文章索引失败: articleId={}, msg={}", articleId, e.getMessage());
                log.debug("ES 删除文章索引异常详情", e);
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    indexExecutor.execute(task);
                }
            });
        } else {
            indexExecutor.execute(task);
        }
    }

    /**
     * 全量重建文章索引。
     *
     * <p>用途：</p>
     * <ul>
     *     <li>初始化 ES 索引（种子数据同步）</li>
     *     <li>修复索引数据不一致问题</li>
     *     <li>索引结构变更后的重建</li>
     * </ul>
     *
     * <p>实现策略：</p>
     * <ol>
     *     <li>确保索引存在（不存在则创建）</li>
     *     <li>清空索引中的旧文档（避免残留/脏数据）</li>
     *     <li>将数据库中已发布文章全量同步到 ES</li>
     * </ol>
     *
     * @return 同步成功的文章数量
     */
    public int rebuildAllArticlesIndex() {
        log.info("开始全量重建文章索引...");

        try {
            indexInitializer.ensureBlogArticleIndex();
        } catch (Exception e) {
            log.warn("ES 索引检查/创建失败: msg={}", e.getMessage());
            log.debug("ES 索引检查/创建异常详情", e);
            return 0;
        }

        try {
            esClient.deleteByQuery(req -> req
                .index(EsIndexNames.BLOG_ARTICLE)
                .conflicts(Conflicts.Proceed)
                .refresh(true)
                .query(q -> q.matchAll(ma -> ma))
            );
            log.info("ES 索引清空完成: {}", EsIndexNames.BLOG_ARTICLE);
        } catch (Exception e) {
            log.warn("ES 索引清空失败: msg={}", e.getMessage());
            log.debug("ES 索引清空异常详情", e);
            return 0;
        }
        
        // 查询所有已发布的文章
        List<BlogArticle> articles = articleMapper.selectList(
            new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getStatus, STATUS_PUBLISHED)
        );
        
        log.info("查询结果: 找到 {} 篇已发布文章 (status={})", articles.size(), STATUS_PUBLISHED);
        
        // 额外调试：查询所有文章看看
        List<BlogArticle> allArticles = articleMapper.selectList(new LambdaQueryWrapper<>());
        log.info("数据库中共有 {} 篇文章", allArticles.size());
        if (!allArticles.isEmpty()) {
            BlogArticle first = allArticles.getFirst();
            log.info("第一篇文章: id={}, title={}, status={}, isDeleted={}", 
                first.getId(), first.getTitle(), first.getStatus(), first.getIsDeleted());
        }
        
        if (articles.isEmpty()) {
            log.info("没有已发布的文章需要同步");
            return 0;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (BlogArticle article : articles) {
            try {
                syncArticle(article.getId());
                successCount++;
                log.debug("同步文章成功: articleId={}, title={}", article.getId(), article.getTitle());
            } catch (Exception e) {
                failCount++;
                log.warn("同步文章失败: articleId={}, title={}, error={}", 
                    article.getId(), article.getTitle(), e.getMessage());
            }
        }
        
        log.info("文章索引重建完成: 总数={}, 成功={}, 失败={}", 
            articles.size(), successCount, failCount);
        
        return successCount;
    }

    /**
     * 关闭 ES 索引同步线程池。
     *
     * <p>避免应用热重启时出现线程泄漏。</p>
     */
    @PreDestroy
    public void shutdownExecutor() {
        indexExecutor.shutdown();
    }

    /**
     * 同步文章索引（核心逻辑）。
     *
     * <p>说明：</p>
     * <ul>
     *     <li>该方法会调用 ES Client，可能抛出 {@link IOException}。</li>
     *     <li>调用方（事务提交后的异步任务）会统一 catch 并记录日志，避免影响主业务。</li>
     * </ul>
     *
     * @param articleId 文章 ID
     * @throws IOException 与 Elasticsearch 通信失败
     */
    public void syncArticle(Long articleId) throws IOException {
        BlogArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            // 文章不存在：视为已删除，清理 ES 索引
            deleteArticleDoc(articleId);
            return;
        }

        // 未发布文章不进入搜索索引：若 ES 中存在旧文档则删除
        if (!Objects.equals(article.getStatus(), STATUS_PUBLISHED)) {
            deleteArticleDoc(articleId);
            return;
        }

        ArticleDocument doc = buildDocument(article);
        // id 同时作为文档 _id：便于 update/delete
        esClient.index(req -> req
                .index(EsIndexNames.BLOG_ARTICLE)
                .id(articleId.toString())
                .document(doc)
        );

        log.info("ES 同步文章完成: articleId={}, index={}", articleId, EsIndexNames.BLOG_ARTICLE);
    }

    /**
     * 删除 ES 文档。
     *
     * @param articleId 文章 ID
     * @throws IOException 与 Elasticsearch 通信失败
     */
    public void deleteArticleDoc(Long articleId) throws IOException {
        if (articleId == null) {
            return;
        }
        // 如果文档不存在，ES 会返回 NOT_FOUND；对学习项目来说直接忽略即可。
        esClient.delete(req -> req
                .index(EsIndexNames.BLOG_ARTICLE)
                .id(articleId.toString())
        );
        log.info("ES 删除文章索引完成: articleId={}, index={}", articleId, EsIndexNames.BLOG_ARTICLE);
    }

    /**
     * 将 MySQL 的文章聚合数据转换为 ES 文档对象。
     *
     * <p>这一步的目的：把搜索需要的字段准备好，写入 ES。</p>
     *
     * @param article 文章实体（blog_article）
     * @return ES 文档
     */
    private ArticleDocument buildDocument(BlogArticle article) {
        ArticleDocument doc = new ArticleDocument();
        doc.setId(article.getId());
        doc.setTitle(article.getTitle());
        doc.setSummary(article.getSummary());
        doc.setSlug(article.getSlug());
        doc.setCoverUrl(article.getCoverUrl());

        doc.setCategoryId(article.getCategoryId());
        doc.setCategoryName(loadCategoryName(article.getCategoryId()));

        doc.setAuthorId(article.getAuthorId());
        doc.setAuthorName(loadAuthorName(article.getAuthorId()));

        doc.setStatus(article.getStatus());
        doc.setPublishTime(article.getPublishTime());
        doc.setCreateTime(article.getCreateTime());

        doc.setReadCount(article.getReadCount());
        doc.setLikeCount(article.getLikeCount());
        doc.setCommentCount(article.getCommentCount());

        // content：用于全文检索（title/summary/content），这里用 Markdown 原文即可
        BlogArticleBody body = bodyMapper.selectOne(new LambdaQueryWrapper<BlogArticleBody>()
                .eq(BlogArticleBody::getArticleId, article.getId()));
        doc.setContent(body == null ? "" : safeString(body.getContentMd()));

        // tags：存标签名称数组（keyword），便于 filters/aggregations
        doc.setTags(loadTagNames(article.getId()));
        return doc;
    }

    /**
     * 查询分类名称。
     *
     * @param categoryId 分类 ID
     * @return 分类名（可能为 null）
     */
    private String loadCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        BlogCategory c = categoryMapper.selectById(categoryId);
        return c == null ? null : safeString(c.getName());
    }

    /**
     * 查询作者名称。
     *
     * <p>优先使用 nickname；若 nickname 为空则回退到 username。</p>
     *
     * @param authorId 作者 ID
     * @return 作者名称（可能为 null）
     */
    private String loadAuthorName(Long authorId) {
        if (authorId == null) {
            return null;
        }
        SysUser u = userMapper.selectById(authorId);
        if (u == null) {
            return null;
        }
        String nickname = u.getNickname();
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }
        return safeString(u.getUsername());
    }

    /**
     * 加载文章标签名称列表。
     *
     * @param articleId 文章 ID
     * @return 标签名称列表（不为 null）
     */
    private List<String> loadTagNames(Long articleId) {
        if (articleId == null) {
            return List.of();
        }

        List<Long> tagIds = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                        .eq(BlogArticleTag::getArticleId, articleId))
                .stream()
                .map(BlogArticleTag::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (tagIds.isEmpty()) {
            return List.of();
        }

        // 批量查询标签，避免 N+1
        return tagMapper.selectList(new LambdaQueryWrapper<BlogTag>().in(BlogTag::getId, tagIds))
                .stream()
                .map(BlogTag::getName)
                .filter(Objects::nonNull)
                .map(this::safeString)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 将字符串安全处理为非 null。
     *
     * @param s 输入
     * @return 非 null 字符串
     */
    private String safeString(String s) {
        return s == null ? "" : s;
    }
}
