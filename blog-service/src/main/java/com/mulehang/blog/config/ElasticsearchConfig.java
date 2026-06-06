package com.mulehang.blog.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Elasticsearch 配置类，手动配置 ElasticsearchClient Bean。
 *
 * <p>通过 {@code blog.elasticsearch.enabled=true} 开启（默认关闭），
 * 避免 ES 不可用时影响应用启动。</p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "blog.elasticsearch.enabled", havingValue = "true")
@EnableConfigurationProperties(BlogElasticsearchProperties.class)
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String uris;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    /**
     * 创建 RestClient Bean（带生命周期管理）。
     *
     * @return RestClient
     */
    @Bean(destroyMethod = "close")
    public RestClient elasticsearchRestClient() {
        String uri = uris.split(",")[0].trim();
        HttpHost host = HttpHost.create(uri);
        log.info("Elasticsearch RestClient 初始化，连接地址：{}", uri);
        RestClientBuilder builder = RestClient.builder(host);
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
            );
            builder.setHttpClientConfigCallback(
                httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
            log.info("Elasticsearch RestClient 已启用 Basic 认证用户：{}", username);
        }
        return builder.build();
    }

    /**
     * 创建 ElasticsearchClient Bean。
     *
     * @param restClient RestClient
     * @return ElasticsearchClient
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        // 配置 ObjectMapper 支持 Java 8 时间类型
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        RestClientTransport transport = new RestClientTransport(
            restClient, 
            new JacksonJsonpMapper(objectMapper)
        );
        log.info("ElasticsearchClient 初始化完成，已配置 JavaTimeModule");
        return new ElasticsearchClient(transport);
    }
}
