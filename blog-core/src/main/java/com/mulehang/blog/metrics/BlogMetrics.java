package com.mulehang.blog.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 博客自定义业务指标。
 */
@Component
public class BlogMetrics {

    private final Counter articlePublishCounter;
    private final Counter commentCounter;
    private final AtomicLong activeUsers;

    /**
     * 初始化自定义指标。
     *
     * <p>
     * 说明：在测试或非监控环境中可能没有 {@link MeterRegistry}，
     * 此时指标将进入“空实现”模式，不影响业务流程。
     * </p>
     *
     * @param registryProvider Micrometer 指标注册表提供者
     */
    public BlogMetrics(ObjectProvider<MeterRegistry> registryProvider) {
        MeterRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            this.articlePublishCounter = null;
            this.commentCounter = null;
            this.activeUsers = new AtomicLong(0);
            return;
        }

        // 文章发布计数器
        this.articlePublishCounter = Counter.builder("blog.article.publish.total")
                .description("Total number of articles published")
                .register(registry);

        // 评论计数器
        this.commentCounter = Counter.builder("blog.comment.total")
                .description("Total number of comments")
                .register(registry);

        // 活跃用户数（Gauge）
        this.activeUsers = new AtomicLong(0);
        Gauge.builder("blog.users.active", activeUsers, AtomicLong::get)
                .description("Number of active users")
                .register(registry);
    }

    /**
     * 增加文章发布计数。
     */
    public void incrementArticlePublish() {
        if (articlePublishCounter != null) {
            articlePublishCounter.increment();
        }
    }

    /**
     * 增加评论计数。
     */
    public void incrementComment() {
        if (commentCounter != null) {
            commentCounter.increment();
        }
    }

    /**
     * 设置活跃用户数。
     *
     * @param count 活跃用户数
     */
    public void setActiveUsers(long count) {
        activeUsers.set(count);
    }
}