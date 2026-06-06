package com.mulehang.blog.mq.task;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.es.ArticleIndexService;
import com.mulehang.blog.es.EsIndexNames;
import com.mulehang.blog.es.document.ArticleDocument;
import com.mulehang.blog.mapper.BlogArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章同步补偿任务。
 *
 * <p><b>为什么需要补偿任务？</b></p>
 * <ul>
 *     <li>MQ 消息可能丢失（虽然概率很低，但不是零）</li>
 *     <li>Consumer 可能宕机，消息堆积在队列里没被消费</li>
 *     <li>ES 可能临时不可用，Consumer 处理失败消息进了死信队列</li>
 *     <li>补偿任务作为"兜底机制"，确保 ES 数据最终和 MySQL 一致</li>
 * </ul>
 *
 * <p><b>补偿策略</b>：</p>
 * <ul>
 *     <li>每小时执行一次</li>
 *     <li>查询最近 24 小时已发布的文章</li>
 *     <li>逐个检查 ES 中是否存在对应文档</li>
 *     <li>不存在则同步（调用 ArticleIndexService.syncArticle）</li>
 *     <li>如果 ES 整体不可用：本轮直接跳过，下次再补偿</li>
 * </ul>
 *
 * <p><b>为什么 ES 不可用时跳过？</b></p>
 * <ul>
 *     <li>如果 ES 挂了，每篇文章都会失败，没必要一直重试</li>
 *     <li>跳过后等 ES 恢复，下一轮补偿任务会处理</li>
 *     <li>避免大量报错日志刷屏</li>
 * </ul>
 *
 * <p><b>条件加载</b>：</p>
 * <p>本任务使用 @ConditionalOnBean(ElasticsearchClient.class)，
 * 只有当 ES 启用时才会创建。ES 未启用时，不需要补偿（也没东西可补偿）。</p>
 *
 * @author mulehang
 * @since 2026-01-16
 */
@Slf4j
@Component
@ConditionalOnBean(ElasticsearchClient.class)
public class ArticleSyncTask {

    /**
     * 文章状态：已发布。
     */
    private static final int STATUS_PUBLISHED = 1;

    /**
     * 文章 Mapper（查询 MySQL）。
     */
    private final BlogArticleMapper articleMapper;

    /**
     * ES 客户端（检查文档是否存在）。
     */
    private final ObjectProvider<ElasticsearchClient> esClientProvider;

    /**
     * 文章索引服务（同步到 ES）。
     */
    private final ObjectProvider<ArticleIndexService> articleIndexServiceProvider;

    /**
     * 构造函数。
     *
     * @param articleMapper                 文章 Mapper
     * @param esClientProvider              ES 客户端提供者
     * @param articleIndexServiceProvider   文章索引服务提供者
     */
    public ArticleSyncTask(BlogArticleMapper articleMapper,
                           ObjectProvider<ElasticsearchClient> esClientProvider,
                           ObjectProvider<ArticleIndexService> articleIndexServiceProvider) {
        this.articleMapper = articleMapper;
        this.esClientProvider = esClientProvider;
        this.articleIndexServiceProvider = articleIndexServiceProvider;
    }

    /**
     * 定时补偿任务：每小时执行一次。
     *
     * <p><b>cron 表达式解释</b>：</p>
     * <p>"0 0 * * * ?" = 每小时的第 0 分 0 秒执行</p>
     * <ul>
     *     <li>第 1 位：秒（0）</li>
     *     <li>第 2 位：分（0）</li>
     *     <li>第 3 位：时（*，每小时）</li>
     *     <li>第 4 位：日（*，每天）</li>
     *     <li>第 5 位：月（*，每月）</li>
     *     <li>第 6 位：周（?，不指定）</li>
     * </ul>
     *
     * <p><b>注意</b>：要让 @Scheduled 生效，需要在启动类或配置类上加 @EnableScheduling</p>
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncMissingArticles() {
        log.info("[补偿任务] 开始执行文章同步补偿任务...");

        // 1. 检查 ES 是否可用
        ElasticsearchClient esClient = esClientProvider.getIfAvailable();
        ArticleIndexService indexService = articleIndexServiceProvider.getIfAvailable();

        if (esClient == null || indexService == null) {
            log.warn("[补偿任务] ES 服务不可用，本轮跳过");
            return;
        }

        // 2. 先 ping 一下 ES，确认连接正常
        if (!isEsAvailable(esClient)) {
            log.warn("[补偿任务] ES 连接失败，本轮跳过，下次再补偿");
            return;
        }

        // 3. 查询最近 24 小时已发布的文章
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<BlogArticle> articles = findPublishedSince(since);

        if (articles.isEmpty()) {
            log.info("[补偿任务] 最近 24 小时没有已发布文章，本轮结束");
            return;
        }

        log.info("[补偿任务] 查询到 {} 篇已发布文章，开始检查 ES 索引...", articles.size());

        // 4. 统计
        int syncCount = 0;
        int skipCount = 0;
        int failCount = 0;

        // 5. 逐个检查并同步
        for (BlogArticle article : articles) {
            try {
                boolean exists = existsInEs(esClient, article.getId());
                if (exists) {
                    // ES 中已存在，跳过
                    skipCount++;
                    continue;
                }

                // ES 中不存在，需要同步
                log.info("[补偿任务] 文章 {} 在 ES 中不存在，开始补偿同步...", article.getId());
                indexService.syncArticle(article.getId());
                syncCount++;
                log.info("[补偿任务] 文章 {} 补偿同步成功", article.getId());

            } catch (Exception e) {
                failCount++;
                log.warn("[补偿任务] 文章 {} 补偿同步失败: {}", article.getId(), e.getMessage());
                log.debug("[补偿任务] 同步异常详情", e);

                // 如果是 ES 整体不可用（比如连接断了），直接跳过后续文章
                if (isEsConnectionError(e)) {
                    log.warn("[补偿任务] 检测到 ES 连接异常，本轮提前结束");
                    break;
                }
            }
        }

        // 6. 输出统计
        log.info("[补偿任务] 执行完成: 总数={}, 跳过={}, 同步={}, 失败={}",
                articles.size(), skipCount, syncCount, failCount);
    }

    /**
     * 查询最近一段时间内已发布的文章。
     *
     * <p><b>查询条件</b>：</p>
     * <ul>
     *     <li>status = 1（已发布）</li>
     *     <li>publishTime >= since（发布时间在指定时间之后）</li>
     * </ul>
     *
     * @param since 起始时间
     * @return 文章列表
     */
    private List<BlogArticle> findPublishedSince(LocalDateTime since) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getStatus, STATUS_PUBLISHED)
               .ge(BlogArticle::getPublishTime, since)
               // 只查必要字段，减少内存占用
               .select(BlogArticle::getId, BlogArticle::getPublishTime);
        return articleMapper.selectList(wrapper);
    }

    /**
     * 检查 ES 是否可用（ping）。
     *
     * @param esClient ES 客户端
     * @return true 表示可用
     */
    private boolean isEsAvailable(ElasticsearchClient esClient) {
        try {
            return esClient.ping().value();
        } catch (Exception e) {
            log.warn("[补偿任务] ES ping 失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查文章在 ES 中是否存在。
     *
     * <p>使用 GET 请求检查文档是否存在，比 search 更高效。</p>
     *
     * @param esClient  ES 客户端
     * @param articleId 文章 ID
     * @return true 表示存在
     */
    private boolean existsInEs(ElasticsearchClient esClient, Long articleId) {
        try {
            GetResponse<ArticleDocument> response = esClient.get(g -> g
                            .index(EsIndexNames.BLOG_ARTICLE)
                            .id(articleId.toString()),
                    ArticleDocument.class
            );
            return response.found();
        } catch (Exception e) {
            // 查询失败视为不存在，让补偿逻辑去同步
            log.debug("[补偿任务] 检查 ES 文档是否存在失败: articleId={}, error={}", articleId, e.getMessage());
            return false;
        }
    }

    /**
     * 判断是否是 ES 连接级别的错误。
     *
     * <p>如果是连接错误（比如网络不通、ES 宕机），后续文章大概率也会失败，
     * 所以提前结束本轮任务，避免无谓的重试。</p>
     *
     * @param e 异常
     * @return true 表示是连接错误
     */
    private boolean isEsConnectionError(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        // 常见的连接错误关键词
        return message.contains("Connection refused")
                || message.contains("Connection reset")
                || message.contains("Connection timed out")
                || message.contains("No route to host")
                || message.contains("java.net.UnknownHostException");
    }
}
