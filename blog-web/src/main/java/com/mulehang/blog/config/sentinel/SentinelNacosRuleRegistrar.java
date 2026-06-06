package com.mulehang.blog.config.sentinel;

import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sentinel Nacos 规则注册器：从 Nacos 动态加载流控/熔断规则并支持热更新。
 *
 * <p>启用方式：{@code sentinel.nacos.enabled=true}，并在 Nacos 中维护 JSON 规则。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sentinel.nacos", name = "enabled", havingValue = "true")
public class SentinelNacosRuleRegistrar {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SentinelNacosProperties nacosProperties;

    /**
     * 注册 Nacos 数据源。
     */
    @PostConstruct
    public void registerNacosDataSources() {
        String serverAddr = nacosProperties.getServerAddr();
        if (nacosProperties.getNamespace() != null && !nacosProperties.getNamespace().isBlank()) {
            System.setProperty("nacos.namespace", nacosProperties.getNamespace());
        }

        NacosDataSource<List<FlowRule>> flowSource = new NacosDataSource<>(
                serverAddr,
                nacosProperties.getGroupId(),
                nacosProperties.getFlowDataId(),
                source -> parseFlowRules(source)
        );
        FlowRuleManager.register2Property(flowSource.getProperty());

        NacosDataSource<List<DegradeRule>> degradeSource = new NacosDataSource<>(
                serverAddr,
                nacosProperties.getGroupId(),
                nacosProperties.getDegradeDataId(),
                source -> parseDegradeRules(source)
        );
        DegradeRuleManager.register2Property(degradeSource.getProperty());

        log.info("Sentinel 规则已从 Nacos 注册: server={}, group={}, flowDataId={}, degradeDataId={}",
                serverAddr, nacosProperties.getGroupId(),
                nacosProperties.getFlowDataId(), nacosProperties.getDegradeDataId());
    }

    /**
     * 解析 Nacos 流控规则 JSON。
     *
     * @param source JSON 字符串
     * @return 流控规则列表
     */
    private List<FlowRule> parseFlowRules(String source) {
        try {
            return OBJECT_MAPPER.readValue(source, new TypeReference<List<FlowRule>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("解析 Nacos 流控规则失败", ex);
        }
    }

    /**
     * 解析 Nacos 熔断规则 JSON。
     *
     * @param source JSON 字符串
     * @return 熔断规则列表
     */
    private List<DegradeRule> parseDegradeRules(String source) {
        try {
            return OBJECT_MAPPER.readValue(source, new TypeReference<List<DegradeRule>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("解析 Nacos 熔断规则失败", ex);
        }
    }
}
