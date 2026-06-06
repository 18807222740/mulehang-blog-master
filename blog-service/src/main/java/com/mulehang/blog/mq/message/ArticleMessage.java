package com.mulehang.blog.mq.message;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章消息体（MQ 传输对象）。
 *
 * <p><b>这个类是什么？</b></p>
 * <p>Producer 发送消息、Consumer 接收消息时，用这个类来承载数据。
 * RabbitMQ 会把它序列化成 JSON（因为我们配了 Jackson2JsonMessageConverter），
 * 所以在 RabbitMQ Management 控制台里可以直接看到消息内容。</p>
 *
 * <p><b>为什么消息体只放最小信息？</b></p>
 * <ul>
 *     <li>消息越小，网络传输越快、队列堆积占用内存越少</li>
 *     <li>Consumer 收到消息后，再根据 articleId 去查数据库获取完整数据</li>
 *     <li>这样即使消息发送后文章又被修改了，Consumer 也能拿到最新数据</li>
 * </ul>
 *
 * <p><b>字段说明</b>：</p>
 * <ul>
 *     <li>articleId：文章 ID，Consumer 用它去查数据库</li>
 *     <li>action：动作类型（UPSERT / DELETE），告诉 Consumer 是同步还是删除</li>
 *     <li>reason：触发原因（create / update / publish / delete），方便日志排查</li>
 *     <li>timestamp：消息产生时间，方便排查消息堆积、延迟等问题</li>
 * </ul>
 *
 * @author mulehang
 * @since 2026-01-16
 */
public class ArticleMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章 ID。
     *
     * <p>Consumer 会根据这个 ID 去数据库查询文章详情</p>
     */
    private Long articleId;

    /**
     * 动作类型。
     *
     * <p>可选值：</p>
     * <ul>
     *     <li>UPSERT：新增或更新（同步到 ES）</li>
     *     <li>DELETE：删除（从 ES 删除）</li>
     * </ul>
     */
    private String action;

    /**
     * 触发原因。
     *
     * <p>可选值：</p>
     * <ul>
     *     <li>create：创建文章时触发</li>
     *     <li>update：更新文章时触发</li>
     *     <li>publish：发布文章时触发</li>
     *     <li>delete：删除文章时触发</li>
     * </ul>
     * <p>这个字段主要用于日志排查，方便知道消息是从哪里触发的</p>
     */
    private String reason;

    /**
     * 消息产生时间。
     *
     * <p>用于排查消息延迟、堆积等问题。
     * 如果发现 Consumer 处理的消息 timestamp 很早，说明消息堆积了。</p>
     */
    private LocalDateTime timestamp;

    /**
     * 无参构造函数（Jackson 反序列化需要）。
     */
    public ArticleMessage() {
    }

    /**
     * 全参构造函数。
     *
     * @param articleId 文章 ID
     * @param action    动作类型（UPSERT / DELETE）
     * @param reason    触发原因（create / update / publish / delete）
     * @param timestamp 消息产生时间
     */
    public ArticleMessage(Long articleId, String action, String reason, LocalDateTime timestamp) {
        this.articleId = articleId;
        this.action = action;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    // ========================================================================
    // Getter / Setter（Jackson 序列化需要）
    // ========================================================================

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ========================================================================
    // toString（日志打印用）
    // ========================================================================

    /**
     * 重写 toString，方便日志打印。
     *
     * <p>示例输出：ArticleMessage{articleId=123, action='UPSERT', reason='create', timestamp=2026-01-16T14:30:00}</p>
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "ArticleMessage{" +
                "articleId=" + articleId +
                ", action='" + action + '\'' +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    // ========================================================================
    // 静态工厂方法（简化创建）
    // ========================================================================

    /**
     * 创建一个 UPSERT 类型的消息（新增或更新）。
     *
     * @param articleId 文章 ID
     * @param reason    触发原因（create / update / publish）
     * @return ArticleMessage
     */
    public static ArticleMessage upsert(Long articleId, String reason) {
        return new ArticleMessage(articleId, "UPSERT", reason, LocalDateTime.now());
    }

    /**
     * 创建一个 DELETE 类型的消息（删除）。
     *
     * @param articleId 文章 ID
     * @return ArticleMessage
     */
    public static ArticleMessage delete(Long articleId) {
        return new ArticleMessage(articleId, "DELETE", "delete", LocalDateTime.now());
    }
}
