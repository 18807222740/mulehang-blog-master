package com.mulehang.blog.service;

import com.mulehang.blog.task.EmailTask;

import java.time.Duration;

/**
 * Redisson 延迟队列（邮件任务）生产者。
 */
public interface DelayedEmailService {

    /**
     * 投递一个延迟执行的邮件任务。
     */
    void enqueue(EmailTask task, Duration delay);
}
