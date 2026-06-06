package com.mulehang.blog.health;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 健康检查指示器
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blog.elasticsearch.enabled", havingValue = "true")
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private final ElasticsearchClient esClient;

    /**
     * 检查 Elasticsearch 连接状态
     *
     * @return 健康状态
     */
    @Override
    public Health health() {
        try {
            boolean ping = esClient.ping().value();
            if (ping) {
                return Health.up()
                        .withDetail("cluster", "connected")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Ping failed")
                        .build();
            }
        } catch (Exception e) {
            log.debug("Elasticsearch 健康检查失败: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
