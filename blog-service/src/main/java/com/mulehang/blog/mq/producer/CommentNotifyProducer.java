package com.mulehang.blog.mq.producer;

import com.mulehang.blog.mq.constant.MqConstants;
import com.mulehang.blog.mq.message.CommentNotifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * 评论通知消息生产者。
 *
 * <p>职责：评论入库成功后，发一条 MQ 消息给消费者，由消费者异步发送邮件。</p>
 *
 * @author mulehang
 * @since 2026-01-17
 */
@Slf4j
@Component
public class CommentNotifyProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造函数。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public CommentNotifyProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送评论通知消息（事务提交后发送）。
     *
     * @param articleId 文章 ID
     * @param commentId 评论 ID
     */
    public void sendNotify(Long articleId, Long commentId) {
        if (articleId == null || commentId == null) {
            log.warn("[MQ] sendNotify 跳过：articleId/commentId 为空，articleId={}, commentId={}", articleId, commentId);
            return;
        }

        CommentNotifyMessage message = CommentNotifyMessage.of(articleId, commentId);

        executeAfterCommit(() -> doSend(
                MqConstants.COMMENT_EXCHANGE,
                MqConstants.ROUTING_KEY_COMMENT_NOTIFY,
                message
        ));
    }

    /**
     * 实际发送消息的方法。
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息体
     */
    private void doSend(String exchange, String routingKey, CommentNotifyMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    exchange,
                    routingKey,
                    message,
                    msg -> {
                        msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return msg;
                    }
            );
            log.info("[MQ] 评论通知消息发送成功: exchange={}, routingKey={}, message={}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("[MQ] 评论通知消息发送失败: exchange={}, routingKey={}, message={}, error={}",
                    exchange, routingKey, message, e.getMessage());
            log.debug("[MQ] 评论通知消息发送异常详情", e);
        }
    }

    /**
     * 在事务提交后执行任务。
     *
     * @param task 要执行的任务
     */
    private void executeAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            log.debug("[MQ] 已注册评论通知 afterCommit 回调，等待事务提交后发送消息");
        } else {
            task.run();
        }
    }
}

