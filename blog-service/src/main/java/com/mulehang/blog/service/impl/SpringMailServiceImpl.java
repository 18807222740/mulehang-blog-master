package com.mulehang.blog.service.impl;

import com.mulehang.blog.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件 Service（Spring Mail 真实实现）。
 *
 * <p>通过 spring.mail.enabled=true 激活此实现。</p>
 * <p>需要配置 spring.mail.host/username/password 等参数。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.enabled", havingValue = "true", matchIfMissing = false)
public class SpringMailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * 发件人邮箱（从配置文件读取）。
     */
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送纯文本邮件。
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    @Override
    public void sendText(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("[邮件发送成功] 收件人={}, 主题={}", to, subject);
        } catch (Exception e) {
            log.error("[邮件发送失败] 收件人={}, 主题={}, 错误={}", to, subject, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 HTML 邮件（扩展功能）。
     *
     * @param to          收件人邮箱
     * @param subject     邮件主题
     * @param htmlContent HTML 内容
     */
    public void sendHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true 表示 HTML 格式

            mailSender.send(mimeMessage);
            log.info("[HTML邮件发送成功] 收件人={}, 主题={}", to, subject);
        } catch (Exception e) {
            log.error("[HTML邮件发送失败] 收件人={}, 主题={}, 错误={}", to, subject, e.getMessage(), e);
            throw new RuntimeException("HTML邮件发送失败: " + e.getMessage(), e);
        }
    }
}
