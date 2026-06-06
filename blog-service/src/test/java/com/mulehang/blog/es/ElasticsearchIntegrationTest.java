package com.mulehang.blog.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.mulehang.blog.config.BlogElasticsearchProperties;
import com.mulehang.blog.config.ElasticsearchConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Elasticsearch Testcontainers 集成测试：验证客户端连接与索引写入。
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = ElasticsearchIntegrationTest.TestConfig.class)
class ElasticsearchIntegrationTest {

    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:8.11.0"
    ).withEnv("xpack.security.enabled", "false");

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    /**
     * 动态注入 ES 连接信息。
     *
     * @param registry 属性注册器
     */
    @DynamicPropertySource
    static void registerElasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("blog.elasticsearch.enabled", () -> "true");
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @Test
    void shouldConnectAndIndexDocument() throws Exception {
        IndexResponse response = elasticsearchClient.index(i -> i
                .index("blog-integration-test")
                .id("1")
                .document(java.util.Map.of("title", "integration-test"))
        );

        assertEquals(Result.Created, response.result());
        assertTrue(response.index().startsWith("blog-integration-test"));
    }

    /**
     * 最小 Spring 测试配置。
     */
    @Configuration
    @EnableConfigurationProperties(BlogElasticsearchProperties.class)
    @ImportAutoConfiguration(JacksonAutoConfiguration.class)
    @Import(ElasticsearchConfig.class)
    static class TestConfig {
    }
}
