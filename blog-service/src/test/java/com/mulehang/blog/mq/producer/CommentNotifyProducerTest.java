package com.mulehang.blog.mq.producer;

import com.mulehang.blog.mq.constant.MqConstants;
import com.mulehang.blog.mq.message.CommentNotifyMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CommentNotifyProducerTest {

    @Test
    void sendNotify_shouldSendMessageWhenNoTransaction() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        CommentNotifyProducer producer = new CommentNotifyProducer(rabbitTemplate);

        producer.sendNotify(1L, 2L);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.COMMENT_EXCHANGE),
                eq(MqConstants.ROUTING_KEY_COMMENT_NOTIFY),
                payloadCaptor.capture(),
                any(MessagePostProcessor.class));

        Object payload = payloadCaptor.getValue();
        assertNotNull(payload);
        CommentNotifyMessage message = (CommentNotifyMessage) payload;
        assertEquals(1L, message.getArticleId());
        assertEquals(2L, message.getCommentId());
        assertNotNull(message.getTimestamp());
    }
}
