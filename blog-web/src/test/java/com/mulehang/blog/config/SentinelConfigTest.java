package com.mulehang.blog.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mulehang.blog.config.sentinel.SentinelRuleLoader;
import com.mulehang.blog.config.sentinel.SentinelRuleProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sentinel 配置测试：验证外部化 YAML 规则是否正确加载。
 */
@DisplayName("Sentinel 配置测试")
class SentinelConfigTest {

    private SentinelRuleLoader ruleLoader;

    /**
     * 每个用例前清空并重新加载规则。
     */
    @BeforeEach
    void setUp() throws Exception {
        FlowRuleManager.loadRules(List.of());
        DegradeRuleManager.loadRules(List.of());
        ruleLoader = new SentinelRuleLoader(loadRulePropertiesFromYaml());
        ruleLoader.loadRules();
    }

    @Test
    @DisplayName("应该成功初始化流控规则")
    void shouldInitFlowRules() {
        List<FlowRule> rules = FlowRuleManager.getRules();

        assertNotNull(rules, "流控规则不应为 null");
        assertEquals(9, rules.size(), "应该加载 9 条流控规则");

        FlowRule aiChatRule = findFlowRule(rules, "ai-chat");
        assertEquals(RuleConstant.FLOW_GRADE_QPS, aiChatRule.getGrade(), "应该使用 QPS 限流");
        assertEquals(10.0, aiChatRule.getCount(), "QPS 限制应该为 10");

        assertEquals(5.0, findFlowRule(rules, "ai-chat-stream").getCount());
        assertEquals(20.0, findFlowRule(rules, "ai-assistant").getCount());
        assertEquals(10.0, findFlowRule(rules, "ai-writing").getCount());
        assertEquals(5.0, findFlowRule(rules, "ai-writing-stream").getCount());
        assertEquals(5.0, findFlowRule(rules, "auth-login").getCount());
        assertEquals(3.0, findFlowRule(rules, "auth-register").getCount());
        assertEquals(10.0, findFlowRule(rules, "comment-create").getCount());
        assertEquals(5.0, findFlowRule(rules, "file-upload").getCount());
    }

    @Test
    @DisplayName("应该成功初始化熔断规则")
    void shouldInitDegradeRules() {
        List<DegradeRule> rules = DegradeRuleManager.getRules();

        assertNotNull(rules, "熔断规则不应为 null");
        assertEquals(5, rules.size(), "应该加载 5 条熔断规则");

        DegradeRule aiChatRule = rules.stream()
                .filter(r -> "ai-chat".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiChatRule, "ai-chat 熔断规则应该存在");
        assertEquals(CircuitBreakerStrategy.ERROR_RATIO.getType(), aiChatRule.getGrade());
        assertEquals(0.5, aiChatRule.getCount());
        assertEquals(30, aiChatRule.getTimeWindow());
        assertEquals(10, aiChatRule.getMinRequestAmount());

        assertEquals(5, findDegradeRule(rules, "ai-chat-stream").getMinRequestAmount());
        assertEquals(0.6, findDegradeRule(rules, "ai-assistant").getCount());
        assertEquals(20, findDegradeRule(rules, "ai-assistant").getTimeWindow());
        assertEquals(10, findDegradeRule(rules, "ai-writing").getMinRequestAmount());
        assertEquals(5, findDegradeRule(rules, "ai-writing-stream").getMinRequestAmount());
    }

    @Test
    @DisplayName("规则应该按资源名称正确分组")
    void shouldGroupRulesByResource() {
        List<FlowRule> flowRules = FlowRuleManager.getRules();
        List<DegradeRule> degradeRules = DegradeRuleManager.getRules();

        String[] aiResources = {"ai-chat", "ai-chat-stream", "ai-assistant", "ai-writing", "ai-writing-stream"};
        for (String resource : aiResources) {
            assertEquals(1, flowRules.stream().filter(r -> resource.equals(r.getResource())).count());
            assertEquals(1, degradeRules.stream().filter(r -> resource.equals(r.getResource())).count());
        }

        String[] extraFlowResources = {"auth-login", "auth-register", "comment-create", "file-upload"};
        for (String resource : extraFlowResources) {
            assertEquals(1, flowRules.stream().filter(r -> resource.equals(r.getResource())).count());
            assertEquals(0, degradeRules.stream().filter(r -> resource.equals(r.getResource())).count());
        }
    }

    @Test
    @DisplayName("流控规则的 QPS 阈值应该符合预期")
    void shouldHaveCorrectQpsThreshold() {
        List<FlowRule> rules = FlowRuleManager.getRules();
        FlowRule streamRule = findFlowRule(rules, "ai-chat-stream");
        FlowRule chatRule = findFlowRule(rules, "ai-chat");
        FlowRule assistantRule = findFlowRule(rules, "ai-assistant");

        assertTrue(streamRule.getCount() < chatRule.getCount());
        assertTrue(chatRule.getCount() < assistantRule.getCount());
    }

    /**
     * 从 classpath 加载 sentinel-rules.yml 中的规则配置。
     *
     * @return 规则属性
     */
    private SentinelRuleProperties loadRulePropertiesFromYaml() throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sentinel-rules.yml")) {
            assertNotNull(inputStream, "sentinel-rules.yml 应存在于 classpath");
            JsonNode root = yamlMapper.readTree(inputStream);
            return yamlMapper.treeToValue(root.path("sentinel").path("rules"), SentinelRuleProperties.class);
        }
    }

    /**
     * 查找流控规则。
     *
     * @param rules    规则列表
     * @param resource 资源名
     * @return 流控规则
     */
    private FlowRule findFlowRule(List<FlowRule> rules, String resource) {
        return rules.stream()
                .filter(r -> resource.equals(r.getResource()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到资源 " + resource + " 的流控规则"));
    }

    /**
     * 查找熔断规则。
     *
     * @param rules    规则列表
     * @param resource 资源名
     * @return 熔断规则
     */
    private DegradeRule findDegradeRule(List<DegradeRule> rules, String resource) {
        return rules.stream()
                .filter(r -> resource.equals(r.getResource()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到资源 " + resource + " 的熔断规则"));
    }
}
