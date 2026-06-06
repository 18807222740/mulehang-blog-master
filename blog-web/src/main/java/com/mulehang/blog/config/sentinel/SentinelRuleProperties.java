package com.mulehang.blog.config.sentinel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 规则外部化配置（本地 YAML；生产可切换 Nacos 动态加载）。
 */
@Data
@ConfigurationProperties(prefix = "sentinel.rules")
public class SentinelRuleProperties {

    /** 流控规则列表 */
    private List<SentinelFlowRuleItem> flow = new ArrayList<>();

    /** 熔断规则列表 */
    private List<SentinelDegradeRuleItem> degrade = new ArrayList<>();
}
