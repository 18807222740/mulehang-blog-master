package com.mulehang.blog.mq;

import com.mulehang.blog.mq.config.RabbitMqConfig;
import com.mulehang.blog.mq.constant.MqConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RabbitMQ Testcontainers 集成测试：验证交换机/队列声明与消息投递。
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = RabbitMqIntegrationTest.TestConfig.class)
class RabbitMqIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 动态注入 RabbitMQ 连接信息。
     *
     * @param registry 属性注册器
     */
    @DynamicPropertySource
    static void registerRabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMq::getHost);
        registry.add("spring.rabbitmq.port", rabbitMq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMq::getAdminPassword);
    }

    @Test
    void shouldDeclareExchangeAndDeliverMessage() {
        String payload = "integration-test-" + System.currentTimeMillis();
        rabbitTemplate.convertAndSend(
                MqConstants.COMMENT_EXCHANGE,
                MqConstants.ROUTING_KEY_COMMENT_NOTIFY,
                payload
        );

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Object received = rabbitTemplate.receiveAndConvert(MqConstants.COMMENT_NOTIFY_QUEUE);
            assertNotNull(received);
            assertEquals(payload, received.toString());
        });
    }

    /**
     * 最小 Spring 测试配置。
     */
    @Configuration
    @ImportAutoConfiguration(RabbitAutoConfiguration.class)
    @Import(RabbitMqConfig.class)
    static class TestConfig {
    }
}
