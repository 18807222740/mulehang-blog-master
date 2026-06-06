package com.mulehang.blog.mq.config;

import com.mulehang.blog.mq.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类。
 *
 * <p><b>这个类做了什么？</b></p>
 * <ul>
 *     <li>声明 Exchange（交换机）：消息的"路由中心"</li>
 *     <li>声明 Queue（队列）：消息的"存储容器"，Consumer 监听它</li>
 *     <li>声明 Binding（绑定）：告诉 Exchange "哪个 routingKey 的消息发到哪个 Queue"</li>
 *     <li>配置死信队列（DLX/DLQ）：处理失败的消息不会丢，会进入死信队列</li>
 * </ul>
 *
 * <p><b>消息流转示意图</b>：</p>
 * <pre>
 *   Producer
 *      │
 *      │ convertAndSend(ARTICLE_EXCHANGE, "article.upsert", message)
 *      ▼
 *   TopicExchange: blog.article.exchange
 *      │
 *      ├─ routingKey = "article.upsert" ──► Queue: blog.article.upsert.queue ──► Consumer (同步 ES)
 *      │
 *      └─ routingKey = "article.delete" ──► Queue: blog.article.delete.queue ──► Consumer (删除 ES 文档)
 *
 *   如果 Consumer 处理失败并 nack(requeue=false)：
 *      │
 *      ▼
 *   DirectExchange: blog.dlx.exchange
 *      │
 *      └─ routingKey = "dlx.article" ──► Queue: blog.dlx.queue（死信队列，等待人工处理）
 * </pre>
 *
 * <p><b>为什么要用死信队列？</b></p>
 * <ul>
 *     <li>避免消息丢失：处理失败的消息有地方存</li>
 *     <li>避免无限重试：如果 requeue=true，消息会一直重试把服务打爆</li>
 *     <li>方便排查：可以从死信队列里捞消息看看是什么导致的失败</li>
 * </ul>
 *
 * @author mulehang
 * @since 2026-01-16
 */
@Configuration
public class RabbitMqConfig {

    // ========================================================================
    // 消息转换器（重要！）
    // ========================================================================

    /**
     * 消息转换器：Java 对象 <-> JSON。
     *
     * <p><b>为什么需要这个？</b></p>
     * <ul>
     *     <li>默认的 RabbitTemplate 用 Java 序列化，生成的消息是二进制，不可读</li>
     *     <li>用 Jackson2JsonMessageConverter 后，消息是 JSON 格式，方便调试和排查</li>
     *     <li>在 RabbitMQ Management 控制台里可以直接看到消息内容</li>
     * </ul>
     *
     * @return Jackson2JsonMessageConverter
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ========================================================================
    // 文章交换机（Topic Exchange）
    // ========================================================================

    /**
     * 文章交换机。
     *
     * <p><b>参数说明</b>：</p>
     * <ul>
     *     <li>durable = true：交换机持久化，RabbitMQ 重启后还在</li>
     *     <li>autoDelete = false：没有队列绑定时不自动删除</li>
     * </ul>
     *
     * @return TopicExchange
     */
    @Bean
    public TopicExchange articleExchange() {
        return new TopicExchange(MqConstants.ARTICLE_EXCHANGE, true, false);
    }

    /**
     * 评论交换机。
     *
     * @return DirectExchange
     */
    @Bean
    public DirectExchange commentExchange() {
        return new DirectExchange(MqConstants.COMMENT_EXCHANGE, true, false);
    }

    // ========================================================================
    // 文章队列（带死信配置）
    // ========================================================================

    /**
     * 文章 upsert 队列（新增/更新 -> 同步 ES）。
     *
     * <p><b>关键配置</b>：</p>
     * <ul>
     *     <li>durable()：队列持久化，RabbitMQ 重启后队列还在、消息还在（前提是消息也要持久化）</li>
     *     <li>deadLetterExchange()：指定死信交换机，消息被 nack 后会转发到这里</li>
     *     <li>deadLetterRoutingKey()：指定死信路由键</li>
     * </ul>
     *
     * @return Queue
     */
    @Bean
    public Queue articleUpsertQueue() {
        return QueueBuilder
                .durable(MqConstants.ARTICLE_UPSERT_QUEUE)
                // 死信配置：消息被拒绝后转发到死信交换机
                .deadLetterExchange(MqConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(MqConstants.DLX_ROUTING_KEY_ARTICLE)
                .build();
    }

    /**
     * 文章 delete 队列（删除 -> 从 ES 删除文档）。
     *
     * @return Queue
     */
    @Bean
    public Queue articleDeleteQueue() {
        return QueueBuilder
                .durable(MqConstants.ARTICLE_DELETE_QUEUE)
                .deadLetterExchange(MqConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(MqConstants.DLX_ROUTING_KEY_ARTICLE)
                .build();
    }

    /**
     * 评论通知队列（评论发布后通知作者）。
     *
     * @return Queue
     */
    @Bean
    public Queue commentNotifyQueue() {
        return QueueBuilder
                .durable(MqConstants.COMMENT_NOTIFY_QUEUE)
                .deadLetterExchange(MqConstants.DLX_EXCHANGE)
                .deadLetterRoutingKey(MqConstants.DLX_ROUTING_KEY_COMMENT)
                .build();
    }

    // ========================================================================
    // 绑定（Binding）：告诉 Exchange 如何路由消息到 Queue
    // ========================================================================

    /**
     * 绑定：article.upsert -> articleUpsertQueue。
     *
     * <p><b>这行代码的意思</b>：</p>
     * <p>当 Producer 发送消息到 articleExchange，且 routingKey = "article.upsert" 时，
     * 消息会被路由到 articleUpsertQueue。</p>
     *
     * @param articleUpsertQueue upsert 队列
     * @param articleExchange    文章交换机
     * @return Binding
     */
    @Bean
    public Binding articleUpsertBinding(Queue articleUpsertQueue, TopicExchange articleExchange) {
        return BindingBuilder
                .bind(articleUpsertQueue)
                .to(articleExchange)
                .with(MqConstants.ROUTING_KEY_ARTICLE_UPSERT);
    }

    /**
     * 绑定：article.delete -> articleDeleteQueue。
     *
     * @param articleDeleteQueue delete 队列
     * @param articleExchange    文章交换机
     * @return Binding
     */
    @Bean
    public Binding articleDeleteBinding(Queue articleDeleteQueue, TopicExchange articleExchange) {
        return BindingBuilder
                .bind(articleDeleteQueue)
                .to(articleExchange)
                .with(MqConstants.ROUTING_KEY_ARTICLE_DELETE);
    }

    /**
     * 绑定：comment.notify -> commentNotifyQueue。
     *
     * @param commentNotifyQueue 评论通知队列
     * @param commentExchange    评论交换机
     * @return Binding
     */
    @Bean
    public Binding commentNotifyBinding(Queue commentNotifyQueue, DirectExchange commentExchange) {
        return BindingBuilder
                .bind(commentNotifyQueue)
                .to(commentExchange)
                .with(MqConstants.ROUTING_KEY_COMMENT_NOTIFY);
    }

    // ========================================================================
    // 死信交换机 + 死信队列（DLX / DLQ）
    // ========================================================================

    /**
     * 死信交换机（Direct Exchange）。
     *
     * <p>用 Direct 而不是 Topic：死信路由通常是精确匹配，不需要通配符</p>
     *
     * @return DirectExchange
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(MqConstants.DLX_EXCHANGE, true, false);
    }

    /**
     * 死信队列。
     *
     * <p>所有处理失败的消息都会堆积在这里。</p>
     * <p>你可以：</p>
     * <ul>
     *     <li>在 RabbitMQ Management 控制台查看死信消息</li>
     *     <li>写一个后台任务定期处理死信</li>
     *     <li>人工确认后重新投递到业务队列</li>
     * </ul>
     *
     * @return Queue
     */
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(MqConstants.DLX_QUEUE).build();
    }

    /**
     * 绑定：dlx.article -> dlxQueue。
     *
     * @param dlxQueue    死信队列
     * @param dlxExchange 死信交换机
     * @return Binding
     */
    @Bean
    public Binding dlxBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder
                .bind(dlxQueue)
                .to(dlxExchange)
                .with(MqConstants.DLX_ROUTING_KEY_ARTICLE);
    }

    /**
     * 绑定：dlx.comment -> dlxQueue。
     *
     * @param dlxQueue    死信队列
     * @param dlxExchange 死信交换机
     * @return Binding
     */
    @Bean
    public Binding dlxCommentBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder
                .bind(dlxQueue)
                .to(dlxExchange)
                .with(MqConstants.DLX_ROUTING_KEY_COMMENT);
    }
}
