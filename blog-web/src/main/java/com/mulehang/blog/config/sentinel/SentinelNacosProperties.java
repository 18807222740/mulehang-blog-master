package com.mulehang.blog.config.sentinel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sentinel Nacos 数据源配置（启用后规则从 Nacos 动态加载并支持热更新）。
 */
@Data
@ConfigurationProperties(prefix = "sentinel.nacos")
public class SentinelNacosProperties {

    /** 是否启用 Nacos 规则数据源 */
    private boolean enabled = false;

    /** Nacos 服务地址，如 127.0.0.1:8848 */
    private String serverAddr = "127.0.0.1:8848";

    /** 命名空间（可选） */
    private String namespace = "";

    /** 配置分组 */
    private String groupId = "SENTINEL_GROUP";

    /** 流控规则 DataId */
    private String flowDataId = "mulehang-blog-flow-rules";

    /** 熔断规则 DataId */
    private String degradeDataId = "mulehang-blog-degrade-rules";
}
