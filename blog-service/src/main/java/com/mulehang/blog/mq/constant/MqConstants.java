package com.mulehang.blog.mq.constant;

/**
 * RabbitMQ 常量定义。
 *
 * <p>
 * <b>为什么要把这些字符串抽成常量？</b>
 * </p>
 * <ul>
 * <li>避免在 Config、Producer、Consumer 里到处写魔法字符串（手一抖就写错）</li>
 * <li>修改队列名/交换机名时只改一处</li>
 * <li>IDE 可以"Find Usages"快速定位所有使用点</li>
 * </ul>
 *
 * <p>
 * <b>命名规范</b>（本项目约定）：
 * </p>
 * <ul>
 * <li>Exchange（交换机）：{业务}.{模块}.exchange</li>
 * <li>Queue（队列）：{业务}.{模块}.{动作}.queue</li>
 * <li>RoutingKey（路由键）：{模块}.{动作}</li>
 * </ul>
 *
 * <p>
 * <b>消息流转路径（帮你理解 RabbitMQ 的核心概念）</b>：
 * </p>
 * 
 * <pre>
 *   Producer
 *      │
 *      │ 发送消息（指定 exchange + routingKey）
 *      ▼
 *   Exchange（交换机）
 *      │
 *      │ 根据 routingKey 和 binding 规则，把消息路由到对应的 Queue
 *      ▼
 *   Queue（队列）
 *      │
 *      │ Consumer 监听队列，拉取消息消费
 *      ▼
 *   Consumer
 * </pre>
 *
 * <p>
 * <b>死信队列（DLX/DLQ）是什么？</b>
 * </p>
 * <ul>
 * <li>当消息被拒绝（basicNack/basicReject）且不重新入队时，会被转发到"死信交换机"</li>
 * <li>死信交换机再把消息路由到"死信队列"</li>
 * <li>这样做的好处：失败的消息不会丢失，可以事后排查、人工处理、或写补偿逻辑</li>
 * </ul>
 *
 * @author mulehang
 * @since 2026-01-16
 */
public final class MqConstants {

    private MqConstants() {
        // 工具类，禁止实例化
    }

    // ========================================================================
    // 文章相关（Article）
    // ========================================================================

    /**
     * 文章交换机（TopicExchange）。
     *
     * <p>
     * 为什么用 Topic 而不是 Direct？
     * </p>
     * <ul>
     * <li>Topic 支持通配符路由（如 article.*），扩展性更好</li>
     * <li>后续如果要加 article.comment、article.like 等，不用改交换机</li>
     * </ul>
     */
    public static final String ARTICLE_EXCHANGE = "blog.article.exchange";

    /**
     * 文章新增/更新队列（用于同步 ES 索引）。
     *
     * <p>
     * upsert = update or insert，表示"有则更新、无则新增"
     * </p>
     */
    public static final String ARTICLE_UPSERT_QUEUE = "blog.article.upsert.queue";

    /**
     * 文章删除队列（用于从 ES 删除索引）。
     */
    public static final String ARTICLE_DELETE_QUEUE = "blog.article.delete.queue";

    /**
     * 文章 upsert 路由键。
     *
     * <p>
     * Producer 发消息时指定这个 routingKey，Exchange 会把消息路由到 ARTICLE_UPSERT_QUEUE
     * </p>
     */
    public static final String ROUTING_KEY_ARTICLE_UPSERT = "article.upsert";

    /**
     * 文章 delete 路由键。
     */
    public static final String ROUTING_KEY_ARTICLE_DELETE = "article.delete";

    // ========================================================================
    // 评论相关（Comment）
    // ========================================================================

    /**
     * 评论交换机（DirectExchange）。
     */
    public static final String COMMENT_EXCHANGE = "blog.comment.exchange";

    /**
     * 评论通知队列（评论发布后通知作者发邮件）。
     */
    public static final String COMMENT_NOTIFY_QUEUE = "blog.comment.notify.queue";

    /**
     * 评论通知路由键。
     */
    public static final String ROUTING_KEY_COMMENT_NOTIFY = "comment.notify";

    // ========================================================================
    // 死信相关（Dead Letter Exchange / Queue）
    // ========================================================================

    /**
     * 死信交换机（DirectExchange）。
     *
     * <p>
     * 当业务队列里的消息被 nack 且不重新入队时，会被转发到这里
     * </p>
     */
    public static final String DLX_EXCHANGE = "blog.dlx.exchange";

    /**
     * 死信队列。
     *
     * <p>
     * 所有"处理失败"的消息最终都会堆积在这里，方便排查问题
     * </p>
     */
    public static final String DLX_QUEUE = "blog.dlx.queue";

    /**
     * 死信路由键（文章相关）。
     */
    public static final String DLX_ROUTING_KEY_ARTICLE = "dlx.article";

    /**
     * 死信路由键（评论相关）。
     */
    public static final String DLX_ROUTING_KEY_COMMENT = "dlx.comment";
}
