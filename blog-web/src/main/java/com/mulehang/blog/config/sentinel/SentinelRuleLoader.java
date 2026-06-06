package com.mulehang.blog.config.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 从外部配置（YAML）加载 Sentinel 流控/熔断规则。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SentinelRuleLoader {

    private final SentinelRuleProperties ruleProperties;

    /**
     * 加载并注册全部规则。
     */
    public void loadRules() {
        loadFlowRules();
        loadDegradeRules();
    }

    /**
     * 加载流控规则。
     */
    public void loadFlowRules() {
        List<SentinelFlowRuleItem> items = ruleProperties.getFlow();
        if (CollectionUtils.isEmpty(items)) {
            log.warn("未配置 sentinel.rules.flow，跳过流控规则加载");
            FlowRuleManager.loadRules(List.of());
            return;
        }
        List<FlowRule> rules = new ArrayList<>(items.size());
        for (SentinelFlowRuleItem item : items) {
            FlowRule rule = new FlowRule();
            rule.setResource(item.getResource());
            rule.setGrade(item.getGrade() > 0 ? item.getGrade() : RuleConstant.FLOW_GRADE_QPS);
            rule.setCount(item.getCount());
            rule.setLimitApp(item.getLimitApp() == null ? "default" : item.getLimitApp());
            rules.add(rule);
        }
        FlowRuleManager.loadRules(rules);
        log.info("已从配置加载 {} 条 Sentinel 流控规则", rules.size());
    }

    /**
     * 加载熔断规则。
     */
    public void loadDegradeRules() {
        List<SentinelDegradeRuleItem> items = ruleProperties.getDegrade();
        if (CollectionUtils.isEmpty(items)) {
            log.warn("未配置 sentinel.rules.degrade，跳过熔断规则加载");
            DegradeRuleManager.loadRules(List.of());
            return;
        }
        List<DegradeRule> rules = new ArrayList<>(items.size());
        for (SentinelDegradeRuleItem item : items) {
            DegradeRule rule = new DegradeRule();
            rule.setResource(item.getResource());
            rule.setGrade(item.getGrade());
            rule.setCount(item.getCount());
            rule.setTimeWindow(item.getTimeWindow());
            rule.setMinRequestAmount(item.getMinRequestAmount());
            rule.setStatIntervalMs(item.getStatIntervalMs());
            rules.add(rule);
        }
        DegradeRuleManager.loadRules(rules);
        log.info("已从配置加载 {} 条 Sentinel 熔断规则", rules.size());
    }
}
