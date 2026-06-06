package com.mulehang.blog.config.sentinel;

import lombok.Data;

/**
 * Sentinel 流控规则配置项（对应 application/sentinel-rules.yml）。
 */
@Data
public class SentinelFlowRuleItem {

    /** 资源名（与 @SentinelResource value 一致） */
    private String resource;

    /** QPS 阈值 */
    private double count = 10;

    /** 限流模式：0=QPS（默认） */
    private int grade = 0;

    /** 限流来源 app，默认 default */
    private String limitApp = "default";
}
