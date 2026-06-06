package com.mulehang.blog.service.article;

import com.mulehang.blog.mq.producer.ArticleMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 文章 MQ 消息发送门面（可选组件封装）。
 */
@Component
@RequiredArgsConstructor
public class ArticleMqFacade {

    private final ObjectProvider<ArticleMessageProducer> articleMessageProducerProvider;

    /**
     * 发送 UPSERT 消息（MQ 未启用时跳过）。
     *
     * @param articleId 文章 ID
     * @param reason    触发原因
     */
    public void sendUpsertIfEnabled(Long articleId, String reason) {
        ArticleMessageProducer producer = articleMessageProducerProvider.getIfAvailable();
        if (producer == null || articleId == null) {
            return;
        }
        producer.sendUpsert(articleId, reason);
    }

    /**
     * 发送 DELETE 消息（MQ 未启用时跳过）。
     *
     * @param articleId 文章 ID
     */
    public void sendDeleteIfEnabled(Long articleId) {
        ArticleMessageProducer producer = articleMessageProducerProvider.getIfAvailable();
        if (producer == null || articleId == null) {
            return;
        }
        producer.sendDelete(articleId);
    }
}
