package com.mulehang.blog.mq.consumer;

import com.mulehang.blog.es.ArticleIndexService;
import com.mulehang.blog.mq.constant.MqConstants;
import com.mulehang.blog.mq.message.ArticleMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 文章消息消费者。
 *
 * <p><b>这个类做什么？</b></p>
 * <p>监听 RabbitMQ 队列，收到消息后：</p>
 * <ul>
 *     <li>UPSERT 消息：调用 ArticleIndexService 同步文章到 ES</li>
 *     <li>DELETE 消息：调用 ArticleIndexService 从 ES 删除文章</li>
 * </ul>
 *
 * <p><b>手动确认（Manual Ack）是什么？</b></p>
 * <p>RabbitMQ 有三种确认模式：</p>
 * <ul>
 *     <li>AUTO（自动确认）：消息发给 Consumer 就算消费成功，即使 Consumer 处理失败也不会重试</li>
 *     <li>NONE（不确认）：基本不用</li>
 *     <li>MANUAL（手动确认）：Consumer 处理完后调用 basicAck/basicNack 告诉 RabbitMQ "我处理完了"</li>
 * </ul>
 * <p>我们用 MANUAL，因为：</p>
 * <ul>
 *     <li>如果 Consumer 处理失败，可以选择重试（requeue=true）或进死信队列（requeue=false）</li>
 *     <li>不会因为 Consumer 崩溃导致消息丢失</li>
 * </ul>
 *
 * <p><b>为什么 nack 时 requeue=false？</b></p>
 * <ul>
 *     <li>如果 requeue=true，消息会重新进队列，Consumer 又会收到，形成无限重试</li>
 *     <li>如果是代码 Bug 导致的失败，无限重试只会把服务打爆</li>
 *     <li>设置 requeue=false，消息会进死信队列（DLQ），方便排查</li>
 *     <li>我们还有"补偿任务"兜底（3.8），不怕漏数据</li>
 * </ul>
 *
 * <p><b>条件加载说明</b>：</p>
 * <p>本 Consumer 使用 @ConditionalOnBean(ArticleIndexService.class)，
 * 只有当 ES 启用（存在 ArticleIndexService Bean）时才会创建。
 * 如果 ES 未启用，Consumer 不会加载，消息会堆积在队列里，
 * 但不会影响主业务，等 ES 启用后消息会被消费。</p>
 *
 * @author mulehang
 * @since 2026-01-16
 */
@Slf4j
@Component
@ConditionalOnBean(ArticleIndexService.class)
public class ArticleMessageConsumer {

    /**
     * ES 索引服务（已有的，我们复用它）。
     *
     * <p>ArticleIndexService 里已经写好了：</p>
     * <ul>
     *     <li>syncArticle(id)：从 DB 查文章，转换成 ArticleDocument，写入 ES</li>
     *     <li>deleteArticleDoc(id)：从 ES 删除文章文档</li>
     * </ul>
     * <p>我们直接调用它，不重复造轮子。</p>
     */
    private final ObjectProvider<ArticleIndexService> articleIndexServiceProvider;

    /**
     * 构造函数。
     *
     * <p>用 ObjectProvider 做"可选注入"，避免 ES 未启用时报错。</p>
     *
     * @param articleIndexServiceProvider ES 索引服务提供者
     */
    public ArticleMessageConsumer(ObjectProvider<ArticleIndexService> articleIndexServiceProvider) {
        this.articleIndexServiceProvider = articleIndexServiceProvider;
    }

    /**
     * 消费文章 UPSERT 消息（新增/更新 -> 同步 ES）。
     *
     * <p><b>处理流程</b>：</p>
     * <ol>
     *     <li>打印日志（收到消息）</li>
     *     <li>校验消息内容</li>
     *     <li>调用 ArticleIndexService.syncArticle(articleId)</li>
     *     <li>成功：basicAck（确认消费成功）</li>
     *     <li>失败：basicNack（拒绝消息，进死信队列）</li>
     * </ol>
     *
     * <p><b>@RabbitListener 参数说明</b>：</p>
     * <ul>
     *     <li>queues：监听的队列名</li>
     *     <li>Channel：RabbitMQ 通道，用于手动 ack/nack</li>
     *     <li>@Header(AmqpHeaders.DELIVERY_TAG)：消息的投递标签，ack/nack 时需要</li>
     * </ul>
     *
     * @param message     消息体（Spring 自动反序列化）
     * @param channel     RabbitMQ 通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = MqConstants.ARTICLE_UPSERT_QUEUE)
    public void handleUpsert(ArticleMessage message,
                             Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        log.info("[MQ Consumer] 收到 UPSERT 消息: {}", message);

        try {
            // 1. 校验消息
            if (message == null || message.getArticleId() == null) {
                log.warn("[MQ Consumer] UPSERT 消息无效（articleId 为空），直接 ack 丢弃");
                // 无效消息直接确认丢弃，避免堵住队列
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. 获取 ES 索引服务
            ArticleIndexService indexService = articleIndexServiceProvider.getIfAvailable();
            if (indexService == null) {
                log.warn("[MQ Consumer] ArticleIndexService 不可用（ES 未启用？），消息进死信队列");
                // ES 不可用，nack 进死信队列，等 ES 恢复后由补偿任务处理
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // 3. 调用已有的 syncArticle 方法
            //    这个方法会：查 DB -> 判断状态 -> 写 ES（已发布）或 删 ES（未发布/不存在）
            indexService.syncArticle(message.getArticleId());

            // 4. 成功，确认消费
            channel.basicAck(deliveryTag, false);
            log.info("[MQ Consumer] UPSERT 处理成功: articleId={}", message.getArticleId());

        } catch (Exception e) {
            log.error("[MQ Consumer] UPSERT 处理失败: message={}, error={}", message, e.getMessage());
            log.debug("[MQ Consumer] UPSERT 异常详情", e);

            // 5. 失败，拒绝消息（进死信队列）
            //    requeue=false：不重新入队，避免无限重试
            //    消息会被转发到死信队列，方便排查
            tryNack(channel, deliveryTag);
        }
    }

    /**
     * 消费文章 DELETE 消息（删除 -> 从 ES 删除文档）。
     *
     * <p><b>处理流程</b>：</p>
     * <ol>
     *     <li>打印日志</li>
     *     <li>校验消息</li>
     *     <li>调用 ArticleIndexService.deleteArticleDoc(articleId)</li>
     *     <li>成功：basicAck</li>
     *     <li>失败：basicNack</li>
     * </ol>
     *
     * @param message     消息体
     * @param channel     RabbitMQ 通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = MqConstants.ARTICLE_DELETE_QUEUE)
    public void handleDelete(ArticleMessage message,
                             Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        log.info("[MQ Consumer] 收到 DELETE 消息: {}", message);

        try {
            // 1. 校验消息
            if (message == null || message.getArticleId() == null) {
                log.warn("[MQ Consumer] DELETE 消息无效（articleId 为空），直接 ack 丢弃");
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. 获取 ES 索引服务
            ArticleIndexService indexService = articleIndexServiceProvider.getIfAvailable();
            if (indexService == null) {
                log.warn("[MQ Consumer] ArticleIndexService 不可用（ES 未启用？），消息进死信队列");
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // 3. 调用已有的 deleteArticleDoc 方法
            indexService.deleteArticleDoc(message.getArticleId());

            // 4. 成功，确认消费
            channel.basicAck(deliveryTag, false);
            log.info("[MQ Consumer] DELETE 处理成功: articleId={}", message.getArticleId());

        } catch (Exception e) {
            log.error("[MQ Consumer] DELETE 处理失败: message={}, error={}", message, e.getMessage());
            log.debug("[MQ Consumer] DELETE 异常详情", e);

            tryNack(channel, deliveryTag);
        }
    }

    /**
     * 尝试 nack 消息（进死信队列）。
     *
     * <p>单独抽出来是因为 basicNack 本身也可能抛异常（比如 channel 已关闭），
     * 需要 catch 住避免影响上层逻辑。</p>
     *
     * @param channel     RabbitMQ 通道
     * @param deliveryTag 消息投递标签
     */
    private void tryNack(Channel channel, long deliveryTag) {
        try {
            // multiple=false：只拒绝当前这条消息
            // requeue=false：不重新入队，消息会进死信队列
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            log.error("[MQ Consumer] basicNack 失败: deliveryTag={}, error={}", deliveryTag, e.getMessage());
        }
    }
}
