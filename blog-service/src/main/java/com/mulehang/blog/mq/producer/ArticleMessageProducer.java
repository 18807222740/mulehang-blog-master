package com.mulehang.blog.mq.producer;

import com.mulehang.blog.mq.constant.MqConstants;
import com.mulehang.blog.mq.message.ArticleMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * 文章消息生产者。
 *
 * <p><b>这个类做什么？</b></p>
 * <p>当文章发生变更（创建/更新/发布/删除）时，调用这个类发送消息到 RabbitMQ。
 * Consumer 监听队列并处理这些消息（比如同步到 ES）。</p>
 *
 * <p><b>核心设计：事务提交后再发消息（afterCommit）</b></p>
 * <ul>
 *     <li>为什么？如果在事务内发消息，一旦事务回滚，消息已经发出去了，会导致数据不一致</li>
 *     <li>举例：你调了 updateArticle()，事务还没提交就发了 MQ 消息，结果事务回滚了，
 *         但 Consumer 已经收到消息去同步 ES 了，ES 里的数据就是错的</li>
 *     <li>解决：用 Spring 的 TransactionSynchronization，在事务 afterCommit 后再发消息</li>
 * </ul>
 *
 * <p><b>消息持久化</b>：</p>
 * <ul>
 *     <li>我们设置了 MessageDeliveryMode.PERSISTENT</li>
 *     <li>这样 RabbitMQ 会把消息写入磁盘，重启后消息不会丢</li>
 *     <li>前提：队列也要是 durable 的（我们在 RabbitMqConfig 里已经配了）</li>
 * </ul>
 *
 * @author mulehang
 * @since 2026-01-16
 */
@Slf4j
@Component
public class ArticleMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造函数注入 RabbitTemplate。
     *
     * <p>RabbitTemplate 是 Spring AMQP 提供的消息发送工具，帮我们封装了底层的 AMQP 协议操作</p>
     *
     * @param rabbitTemplate Spring 自动配置的 RabbitTemplate
     */
    public ArticleMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送文章 UPSERT 消息（新增/更新）。
     *
     * <p><b>调用时机</b>：</p>
     * <ul>
     *     <li>createArticle()：创建文章后</li>
     *     <li>updateArticle()：更新文章后</li>
     *     <li>publishArticle()：发布文章后</li>
     * </ul>
     *
     * <p><b>消息流转</b>：</p>
     * <pre>
     *   Producer.sendUpsert()
     *      │
     *      ▼
     *   Exchange: blog.article.exchange
     *      │ routingKey = "article.upsert"
     *      ▼
     *   Queue: blog.article.upsert.queue
     *      │
     *      ▼
     *   Consumer: 收到消息，查数据库，同步到 ES
     * </pre>
     *
     * @param articleId 文章 ID
     * @param reason    触发原因（create / update / publish），用于日志排查
     */
    public void sendUpsert(Long articleId, String reason) {
        if (articleId == null) {
            log.warn("[MQ] sendUpsert 跳过：articleId 为空");
            return;
        }

        ArticleMessage message = ArticleMessage.upsert(articleId, reason);

        // 关键：事务提交后再发消息，避免事务回滚但消息已发的问题
        executeAfterCommit(() -> doSend(
                MqConstants.ARTICLE_EXCHANGE,
                MqConstants.ROUTING_KEY_ARTICLE_UPSERT,
                message
        ));
    }

    /**
     * 发送文章 DELETE 消息（删除）。
     *
     * <p><b>调用时机</b>：deleteArticle() 删除文章后</p>
     *
     * <p><b>Consumer 收到后</b>：从 ES 删除该文章的索引</p>
     *
     * @param articleId 文章 ID
     */
    public void sendDelete(Long articleId) {
        if (articleId == null) {
            log.warn("[MQ] sendDelete 跳过：articleId 为空");
            return;
        }

        ArticleMessage message = ArticleMessage.delete(articleId);

        executeAfterCommit(() -> doSend(
                MqConstants.ARTICLE_EXCHANGE,
                MqConstants.ROUTING_KEY_ARTICLE_DELETE,
                message
        ));
    }

    /**
     * 实际发送消息的方法。
     *
     * <p><b>消息属性设置</b>：</p>
     * <ul>
     *     <li>messageId：唯一标识，方便排查重复消息、追踪消息链路</li>
     *     <li>deliveryMode = PERSISTENT：消息持久化，RabbitMQ 重启后不丢</li>
     * </ul>
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息体
     */
    private void doSend(String exchange, String routingKey, ArticleMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    exchange,
                    routingKey,
                    message,
                    // MessagePostProcessor：在消息发送前设置一些属性
                    msg -> {
                        // 设置消息 ID（UUID），方便日志追踪
                        msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                        // 设置消息持久化（重要！）
                        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return msg;
                    }
            );

            log.info("[MQ] 消息发送成功: exchange={}, routingKey={}, message={}",
                    exchange, routingKey, message);

        } catch (Exception e) {
            // 发送失败只记录日志，不影响主业务
            // 因为我们还有"补偿任务"兜底（3.8）
            log.error("[MQ] 消息发送失败: exchange={}, routingKey={}, message={}, error={}",
                    exchange, routingKey, message, e.getMessage());
            log.debug("[MQ] 消息发送异常详情", e);
        }
    }

    /**
     * 在事务提交后执行任务。
     *
     * <p><b>这个方法是整个 Producer 最核心的设计</b>：</p>
     * <ul>
     *     <li>如果当前有事务：注册一个 afterCommit 回调，事务提交后才执行</li>
     *     <li>如果当前没有事务：直接执行（比如在非 @Transactional 方法里调用）</li>
     * </ul>
     *
     * <p><b>为什么这么做？</b></p>
     * <p>假设 ArticleServiceImpl.updateArticle() 里：</p>
     * <pre>
     *   1. articleMapper.updateById(...)  // DB 操作
     *   2. producer.sendUpsert(...)       // 发 MQ 消息
     *   3. 抛异常，事务回滚
     * </pre>
     * <p>如果第 2 步直接发消息，消息已经进 RabbitMQ 了，但第 3 步事务回滚了，
     * Consumer 收到消息去查数据库，要么查不到、要么查到旧数据，导致 ES 和 MySQL 不一致。</p>
     * <p>用 afterCommit 后，只有事务真正提交成功了，才会发消息，问题解决。</p>
     *
     * @param task 要执行的任务
     */
    private void executeAfterCommit(Runnable task) {
        // 判断当前是否有活跃的事务
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 有事务：注册 afterCommit 回调
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            log.debug("[MQ] 已注册 afterCommit 回调，等待事务提交后发送消息");
        } else {
            // 没有事务：直接执行
            task.run();
        }
    }
}
