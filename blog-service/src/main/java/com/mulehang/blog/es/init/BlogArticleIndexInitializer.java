package com.mulehang.blog.es.init;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.mulehang.blog.es.EsIndexNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 文章索引初始化器。
 *
 * <p>为什么需要这个类？</p>
 * <ul>
 *     <li>Elasticsearch 的索引（Index）需要先创建，才能写入/搜索文档（Document）。</li>
 *     <li>索引的 mapping 相当于“字段类型约束”，提前定义可以避免后续写入出现类型冲突。</li>
 * </ul>
 *
 * <p>什么时候执行？</p>
 * <ul>
 *     <li>实现 {@link ApplicationRunner}：Spring Boot 应用启动完成后会执行一次 {@link #run(ApplicationArguments)}。</li>
 * </ul>
 *
 * <p>注意：</p>
 * <ul>
 *     <li>本项目的 ES 属于“可选组件”（参考开发指南），因此这里采取“尽量不影响启动”的策略：
 *     如果 ES 没启动或网络不可达，记录 warn 并跳过初始化。</li>
 *     <li>mapping 中优先使用 IK 分词器（ik_max_word/ik_smart）；若 ES 未安装 IK，会回退到 standard 分词器创建索引。</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnBean(ElasticsearchClient.class)
@RequiredArgsConstructor
public class BlogArticleIndexInitializer implements ApplicationRunner {

    private static final String ANALYZER_IK_MAX_WORD = "ik_max_word";
    private static final String ANALYZER_IK_SMART = "ik_smart";
    private static final String ANALYZER_STANDARD = "standard";

    /**
     * Elasticsearch 官方 Java Client。
     * <p>
     * 由 Spring Boot 基于 {@code spring.elasticsearch.*} 自动装配：
     * 配置位于运行模块（blog-web）的 {@code blog-web/src/main/resources/application.yml}。
     * </p>
     */
    private final ElasticsearchClient esClient;

    /**
     * 应用启动后执行索引初始化。
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureBlogArticleIndex();
        } catch (Exception e) {
            // ES 是可选组件：这里不抛异常中断启动，只记录日志。
            log.warn("跳过 ES 索引初始化：无法连接到 Elasticsearch（请确认已启动并配置 spring.elasticsearch.uris），msg={}", e.getMessage());
            log.debug("ES 索引初始化异常详情", e);
        }
    }

    /**
     * 确保文章索引存在：不存在则创建。
     *
     * <p>该方法会做两件事：</p>
     * <ol>
     *     <li>调用 {@code indices.exists} 判断索引是否存在</li>
     *     <li>不存在时调用 {@code indices.create} 创建索引，并写入 mapping</li>
     * </ol>
     *
     * @throws IOException 与 ES 通信失败
     */
    public void ensureBlogArticleIndex() throws IOException {
        BooleanResponse exists = esClient.indices().exists(req -> req.index(EsIndexNames.BLOG_ARTICLE));
        if (exists.value()) {
            log.info("ES 索引已存在，无需创建：{}", EsIndexNames.BLOG_ARTICLE);
            return;
        }

        // 优先使用 IK 分词器，若插件缺失则回退到 standard
        try {
            CreateIndexResponse resp = createIndexWithAnalyzer(ANALYZER_IK_MAX_WORD, ANALYZER_IK_SMART);
            log.info("ES 索引创建完成（IK）：index={}, acknowledged={}, shardsAcknowledged={}",
                    EsIndexNames.BLOG_ARTICLE, resp.acknowledged(), resp.shardsAcknowledged());
        } catch (Exception e) {
            log.warn("ES 索引创建失败（IK 分词器不可用，尝试回退到 standard），msg={}", e.getMessage());
            CreateIndexResponse resp = createIndexWithAnalyzer(ANALYZER_STANDARD, ANALYZER_STANDARD);
            log.info("ES 索引创建完成（standard）：index={}, acknowledged={}, shardsAcknowledged={}",
                    EsIndexNames.BLOG_ARTICLE, resp.acknowledged(), resp.shardsAcknowledged());
        }
    }

    /**
     * 使用指定分词器创建索引。
     *
     * @param analyzer        索引分词器
     * @param searchAnalyzer  搜索分词器
     * @return 创建索引响应
     * @throws IOException 与 ES 通信失败
     */
    private CreateIndexResponse createIndexWithAnalyzer(String analyzer, String searchAnalyzer) throws IOException {
        return esClient.indices().create(req -> req
                .index(EsIndexNames.BLOG_ARTICLE)
                // settings：学习项目默认 1 分片 + 0 副本，减少资源占用（生产环境请按实际调整）
                .settings(s -> s
                        .numberOfShards("1")
                        .numberOfReplicas("0"))
                .mappings(m -> m
                        // ===== 主键/元信息 =====
                        // 与 MySQL blog_article.id 一致，同时建议作为 ES 文档 _id。
                        .properties("id", p -> p.long_(t -> t))

                        // ===== 全文检索字段（text）=====
                        // title：权重最高（搜索时会设置 title^3）。
                        .properties("title", p -> p.text(t -> t.analyzer(analyzer).searchAnalyzer(searchAnalyzer)))
                        // summary：摘要（搜索时会设置 summary^2）。
                        .properties("summary", p -> p.text(t -> t.analyzer(analyzer).searchAnalyzer(searchAnalyzer)))
                        // content：正文（权重最低）。
                        .properties("content", p -> p.text(t -> t.analyzer(analyzer).searchAnalyzer(searchAnalyzer)))

                        // ===== 结构化字段（keyword/number/date）=====
                        .properties("slug", p -> p.keyword(t -> t))
                        .properties("coverUrl", p -> p.keyword(t -> t))

                        .properties("categoryId", p -> p.long_(t -> t))
                        .properties("categoryName", p -> p.keyword(t -> t))

                        // tags：keyword 数组（用于过滤/聚合，避免 text 分词导致不可控匹配）
                        .properties("tags", p -> p.keyword(t -> t))

                        .properties("authorId", p -> p.long_(t -> t))
                        .properties("authorName", p -> p.keyword(t -> t))

                        .properties("status", p -> p.integer(t -> t))

                        // 日期字段：这里同时兼容两类常见格式：
                        // 1) yyyy-MM-dd HH:mm:ss（很多项目会这么序列化 LocalDateTime）
                        // 2) strict_date_optional_time（ES 默认支持的 ISO8601，如 2026-01-15T11:00:00）
                        // 3) epoch_millis（毫秒时间戳）
                        .properties("publishTime", p -> p.date(t -> t.format("yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis")))
                        .properties("createTime", p -> p.date(t -> t.format("yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis")))

                        .properties("readCount", p -> p.long_(t -> t))
                        .properties("likeCount", p -> p.integer(t -> t))
                        .properties("commentCount", p -> p.integer(t -> t))
                )
        );
    }
}