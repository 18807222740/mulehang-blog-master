package com.mulehang.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 博客 Elasticsearch 配置属性。
 */
@Data
@ConfigurationProperties(prefix = "blog.elasticsearch")
public class BlogElasticsearchProperties {

    /**
     * 是否启用 Elasticsearch（默认 false）。
     */
    private boolean enabled = false;
}
