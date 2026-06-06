package com.mulehang.blog.service.impl;

import com.mulehang.blog.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 邮件 Service（日志模拟实现）。
 *
 * <p>作为 Redisson 延迟队列的消费端示例，这里用日志模拟发信。</p>
 * <p>仅在 spring.mail.enabled=false 或未配置时生效，作为降级方案。</p>
 * <p>若需真实发信，请配置 spring.mail.* 参数并设置 spring.mail.enabled=true。</p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LogEmailServiceImpl implements EmailService {

    /**
     * 发送纯文本邮件。
     *
     * <p>当前实现仅记录日志，不实际发送邮件。</p>
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    @Override
    public void sendText(String to, String subject, String content) {
        log.info("[邮件] 收件人={}, 主题={}, 内容={}", to, subject, content);
    }
}
