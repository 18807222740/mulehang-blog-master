package com.mulehang.blog.service;

/**
 * 邮件发送服务。
 */
public interface EmailService {

    /**
     * 发送纯文本邮件。
     */
    void sendText(String to, String subject, String content);
}
