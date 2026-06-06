package com.mulehang.blog.config.sentinel;

import lombok.Data;

/**
 * Sentinel 熔断降级规则配置项。
 */
@Data
public class SentinelDegradeRuleItem {

    /** 资源名 */
    private String resource;

    /** 熔断策略：0=慢调用比例，1=异常比例，2=异常数 */
    private int grade = 1;

    /** 阈值（错误率 0~1 或异常数） */
    private double count = 0.5;

    /** 熔断时长（秒） */
    private int timeWindow = 30;

    /** 最小请求数 */
    private int minRequestAmount = 10;

    /** 统计窗口（毫秒） */
    private int statIntervalMs = 1000;
}
