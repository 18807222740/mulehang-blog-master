package com.mulehang.blog.config;

import com.mulehang.blog.config.sentinel.SentinelNacosProperties;
import com.mulehang.blog.config.sentinel.SentinelRuleLoader;
import com.mulehang.blog.config.sentinel.SentinelRuleProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 流控和熔断配置入口。
 *
 * <p>默认从 {@code sentinel-rules.yml} 加载规则；启用 {@code sentinel.nacos.enabled=true} 后
 * 由 {@link com.mulehang.blog.config.sentinel.SentinelNacosRuleRegistrar} 从 Nacos 动态加载。</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({SentinelRuleProperties.class, SentinelNacosProperties.class})
public class SentinelConfig {

    private final SentinelRuleLoader ruleLoader;
    private final SentinelNacosProperties nacosProperties;

    /**
     * 初始化 Sentinel 规则（Nacos 未启用时使用本地 YAML）。
     */
    @PostConstruct
    public void initRules() {
        if (nacosProperties.isEnabled()) {
            log.info("Sentinel Nacos 数据源已启用，规则由 Nacos 动态加载");
            return;
        }
        ruleLoader.loadRules();
        log.info("Sentinel 流控和熔断规则初始化完成（本地配置）");
    }
}
